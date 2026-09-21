-- 1. Create Profiles Table (if not exists)
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

-- 2. Create Groups Table
CREATE TABLE IF NOT EXISTS public.groups (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    display_id TEXT UNIQUE NOT NULL, -- Human friendly short ID
    name TEXT NOT NULL,
    description TEXT,
    owner_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    focus_area TEXT,
    member_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Create Group Members Table
CREATE TABLE IF NOT EXISTS public.group_members (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    role TEXT DEFAULT 'member' CHECK (role IN ('admin', 'moderator', 'member')),
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(group_id, user_id)
);

-- 4. Create Group Papers Table (for Shelf/Voting)
CREATE TABLE IF NOT EXISTS public.group_papers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    bibcode TEXT NOT NULL,
    added_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    vote_count INT DEFAULT 0,
    added_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(group_id, bibcode)
);

-- 5. Create Votes Table (Individual votes tracking)
CREATE TABLE IF NOT EXISTS public.group_paper_votes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_paper_id UUID REFERENCES public.group_papers(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(group_paper_id, user_id)
);

-- 6. Create Group Presentations Table (for Calendar)
CREATE TABLE IF NOT EXISTS public.group_presentations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    group_id UUID REFERENCES public.groups(id) ON DELETE CASCADE NOT NULL,
    bibcode TEXT NOT NULL,
    presenter_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. Trigger to maintain vote_count
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

CREATE TRIGGER on_group_paper_vote_change
AFTER INSERT OR DELETE ON public.group_paper_votes
FOR EACH ROW EXECUTE FUNCTION public.handle_group_paper_vote_change();

-- 8. Trigger to maintain member_count
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

CREATE TRIGGER on_group_member_change
AFTER INSERT OR DELETE ON public.group_members
FOR EACH ROW EXECUTE FUNCTION public.handle_group_member_change();

-- 9. Row Level Security (RLS) Policies

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_papers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_paper_votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.group_presentations ENABLE ROW LEVEL SECURITY;

-- Profiles: Users can read all profiles (for member lists) but only edit their own
CREATE POLICY "Public profiles are viewable by everyone" ON public.profiles FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Groups: Viewable if you are a member
CREATE POLICY "Groups are viewable by members" ON public.groups
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = groups.id AND group_members.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can create groups" ON public.groups FOR INSERT WITH CHECK (auth.uid() = owner_id);

-- Group Members: Viewable by members of the same group
CREATE POLICY "Members viewable by group members" ON public.group_members
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members AS m
            WHERE m.group_id = group_members.group_id AND m.user_id = auth.uid()
        )
    );

-- Group Papers: Viewable and manageable by members
CREATE POLICY "Papers viewable by group members" ON public.group_papers
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = group_papers.id AND group_members.user_id = auth.uid()
        )
    );

CREATE POLICY "Members can add papers" ON public.group_papers
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = group_papers.group_id AND group_members.user_id = auth.uid()
        )
    );

-- Votes: Members can see votes and manage their own
CREATE POLICY "Votes viewable by group members" ON public.group_paper_votes
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            JOIN public.group_papers ON group_members.group_id = group_papers.group_id
            WHERE group_papers.id = group_paper_votes.group_paper_id AND group_members.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can vote" ON public.group_paper_votes
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can remove vote" ON public.group_paper_votes
    FOR DELETE USING (auth.uid() = user_id);

-- Presentations: Viewable and manageable by members
CREATE POLICY "Presentations viewable by group members" ON public.group_presentations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.group_members
            WHERE group_members.group_id = group_presentations.group_id AND group_members.user_id = auth.uid()
        )
    );
