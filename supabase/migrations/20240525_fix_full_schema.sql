-- ASTROXPLORE FULL SCHEMA MIGRATION
-- This script ensures all tables for Profiles, Library, Keywords, and Groups exist.

-- 1. Profiles
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT,
    first_name TEXT,
    last_name TEXT,
    institution TEXT,
    affiliation_type TEXT,
    affiliation_name TEXT,
    country TEXT,
    education_level TEXT,
    orcid_id TEXT,
    is_onboarded BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Keywords & User Preferences
CREATE TABLE IF NOT EXISTS public.keywords (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    category TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.user_preferences (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    keyword_tag TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, keyword_tag)
);

-- 3. Library (Saved Papers)
CREATE TABLE IF NOT EXISTS public.saved_papers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    bibcode TEXT NOT NULL,
    title TEXT,
    authors TEXT,
    abstract TEXT,
    category TEXT,
    date_display TEXT,
    citation_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, bibcode)
);

-- 4. Journal Clubs (Groups)
CREATE TABLE IF NOT EXISTS public.groups (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    display_id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    owner_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    focus_area TEXT,
    member_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.group_members (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    role TEXT DEFAULT 'member' CHECK (role IN ('admin', 'moderator', 'member')),
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(group_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.group_papers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    bibcode TEXT NOT NULL,
    added_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    vote_count INT DEFAULT 0,
    added_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(group_id, bibcode)
);

CREATE TABLE IF NOT EXISTS public.group_paper_votes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_paper_id UUID REFERENCES public.group_papers(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(group_paper_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.group_presentations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    bibcode TEXT NOT NULL,
    presenter_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. TRIGGERS
-- Maintain member_count
CREATE OR REPLACE FUNCTION public.handle_group_member_change()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE public.groups SET member_count = member_count + 1 WHERE id = NEW.group_id;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE public.groups SET member_count = member_count - 1 WHERE id = OLD.group_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_group_member_change ON public.group_members;
CREATE TRIGGER on_group_member_change
AFTER INSERT OR DELETE ON public.group_members
FOR EACH ROW EXECUTE FUNCTION public.handle_group_member_change();

-- Maintain vote_count
CREATE OR REPLACE FUNCTION public.handle_group_paper_vote_change()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE public.group_papers SET vote_count = vote_count + 1 WHERE id = NEW.group_paper_id;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE public.group_papers SET vote_count = vote_count - 1 WHERE id = OLD.group_paper_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_group_paper_vote_change ON public.group_paper_votes;
CREATE TRIGGER on_group_paper_vote_change
AFTER INSERT OR DELETE ON public.group_paper_votes
FOR EACH ROW EXECUTE FUNCTION public.handle_group_paper_vote_change();

-- 6. RLS POLICIES
-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.keywords ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.saved_papers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_papers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_paper_votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_presentations ENABLE ROW LEVEL SECURITY;

-- Profiles
DROP POLICY IF EXISTS "Public profiles are viewable by everyone" ON public.profiles;
CREATE POLICY "Public profiles are viewable by everyone" ON public.profiles FOR SELECT USING (true);
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Keywords (Read-only for users)
DROP POLICY IF EXISTS "Keywords are viewable by everyone" ON public.keywords;
CREATE POLICY "Keywords are viewable by everyone" ON public.keywords FOR SELECT USING (true);

-- User Preferences
DROP POLICY IF EXISTS "Users can manage own preferences" ON public.user_preferences;
CREATE POLICY "Users can manage own preferences" ON public.user_preferences
    USING (auth.uid() = user_id);

-- Saved Papers
DROP POLICY IF EXISTS "Users can manage own library" ON public.saved_papers;
CREATE POLICY "Users can manage own library" ON public.saved_papers
    USING (auth.uid() = user_id);

-- Groups
DROP POLICY IF EXISTS "Groups are viewable by members" ON public.groups;
CREATE POLICY "Groups are viewable by members" ON public.groups
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = groups.id AND group_members.user_id = auth.uid()
        )
    );
DROP POLICY IF EXISTS "Users can create groups" ON public.groups;
CREATE POLICY "Users can create groups" ON public.groups FOR INSERT WITH CHECK (auth.uid() = owner_id);

-- Group Members
DROP POLICY IF EXISTS "Members viewable by group members" ON public.group_members;
CREATE POLICY "Members viewable by group members" ON public.group_members
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members AS m
            WHERE m.group_id = group_members.group_id AND m.user_id = auth.uid()
        )
    );

-- Group Papers
DROP POLICY IF EXISTS "Papers viewable by group members" ON public.group_papers;
CREATE POLICY "Papers viewable by group members" ON public.group_papers
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = group_papers.group_id AND group_members.user_id = auth.uid()
        )
    );
DROP POLICY IF EXISTS "Members can add papers" ON public.group_papers;
CREATE POLICY "Members can add papers" ON public.group_papers
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = group_papers.group_id AND group_members.user_id = auth.uid()
        )
    );

-- Votes
DROP POLICY IF EXISTS "Votes viewable by group members" ON public.group_paper_votes;
CREATE POLICY "Votes viewable by group members" ON public.group_paper_votes
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            JOIN public.group_papers ON group_members.group_id = group_papers.group_id
            WHERE group_papers.id = group_paper_votes.group_paper_id AND group_members.user_id = auth.uid()
        )
    );
DROP POLICY IF EXISTS "Users can vote" ON public.group_paper_votes;
CREATE POLICY "Users can vote" ON public.group_paper_votes FOR INSERT WITH CHECK (auth.uid() = user_id);
DROP POLICY IF EXISTS "Users can remove vote" ON public.group_paper_votes;
CREATE POLICY "Users can remove vote" ON public.group_paper_votes FOR DELETE USING (auth.uid() = user_id);

-- Presentations
DROP POLICY IF EXISTS "Presentations viewable by group members" ON public.group_presentations;
CREATE POLICY "Presentations viewable by group members" ON public.group_presentations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = group_presentations.group_id AND group_members.user_id = auth.uid()
        )
    );
