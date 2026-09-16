package com.example.astroxplore.features.profile.data

import com.example.astroxplore.core.database.dao.KeywordDao
import com.example.astroxplore.core.database.entity.KeywordEntity
import com.example.astroxplore.core.database.entity.UserPreferenceEntity
import com.example.astroxplore.features.profile.model.KeywordModel
import com.example.astroxplore.features.profile.model.ProfileModel
import com.example.astroxplore.features.profile.model.UserPreference
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val keywordDao: KeywordDao
) {
    suspend fun getProfile(userId: String): ProfileModel? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["profiles"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<ProfileModel>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(profile: ProfileModel) = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["profiles"].upsert(profile)
    }

    fun getLocalKeywords(): Flow<List<String>> = keywordDao.getAllKeywords().map { entities ->
        entities.map { it.name }
    }

    suspend fun searchKeywordsOnline(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            supabaseClient.postgrest["keywords"]
                .select(columns = Columns.ALL) {
                    filter {
                        ilike("name", "%$query%")
                    }
                    limit(20)
                }
                .decodeList<KeywordModel>()
                .map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRandomKeywordsOnline(limit: Int = 20): List<String> = withContext(Dispatchers.IO) {
        try {
            // Note: Postgrest doesn't have a direct random(), we'll fetch a batch and shuffle
            // In a production app with thousands of rows, we'd use a Postgres Function (RPC)
            supabaseClient.postgrest["keywords"]
                .select(columns = Columns.ALL) {
                    limit(100) 
                }
                .decodeList<KeywordModel>()
                .shuffled()
                .take(limit)
                .map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun syncAvailableKeywords() = withContext(Dispatchers.IO) {
        try {
            val remoteKeywords = supabaseClient.postgrest["keywords"]
                .select(columns = Columns.ALL)
                .decodeList<KeywordModel>()
            
            if (remoteKeywords.isNotEmpty()) {
                keywordDao.clearKeywords()
                keywordDao.insertKeywords(remoteKeywords.map { KeywordEntity(it.name, it.category) })
            }
        } catch (e: Exception) {
            // Log error or handle
        }
    }

    suspend fun getUserPreferences(userId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val remotePrefs = supabaseClient.postgrest["user_preferences"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserPreference>()
                .map { it.keywordTag }
            
            if (remotePrefs.isNotEmpty()) {
                keywordDao.syncUserPreferences(remotePrefs.map { UserPreferenceEntity(it) })
            }
            remotePrefs
        } catch (e: Exception) {
            // Fallback to local if remote fails
            keywordDao.getUserPreferences().first().map { it.keyword }
        }
    }

    fun getLocalUserPreferences(): Flow<List<String>> = keywordDao.getUserPreferences().map { entities ->
        entities.map { it.keyword }
    }

    suspend fun syncUserPreferences(userId: String, keywordTags: List<String>) = withContext(Dispatchers.IO) {
        // Ensure profile exists first
        val profile = getProfile(userId)
        if (profile == null) {
            val userEmail = supabaseClient.auth.currentUserOrNull()?.email ?: ""
            supabaseClient.postgrest["profiles"].upsert(mapOf(
                "id" to userId,
                "email" to userEmail,
                "is_onboarded" to true
            ))
        } else {
            // Mark as onboarded in profile
            supabaseClient.postgrest["profiles"].update({
                set("is_onboarded", true)
            }) {
                filter {
                    eq("id", userId)
                }
            }
        }

        // First delete existing preferences for the user in Supabase
        supabaseClient.postgrest["user_preferences"].delete {
            filter {
                eq("user_id", userId)
            }
        }
        
        // Then insert new ones in Supabase
        val preferences = keywordTags.map { tag ->
            UserPreference(userId = userId, keywordTag = tag)
        }
        if (preferences.isNotEmpty()) {
            supabaseClient.postgrest["user_preferences"].insert(preferences)
        }

        // Sync to local Room
        keywordDao.syncUserPreferences(keywordTags.map { UserPreferenceEntity(it) })
    }

    /**
     * Seeds the database with some default astronomy keywords if online table is empty.
     */
    suspend fun seedKeywordsIfEmpty() = withContext(Dispatchers.IO) {
        try {
            val remoteKeywords = supabaseClient.postgrest["keywords"]
                .select(columns = Columns.ALL)
                .decodeList<KeywordModel>()
            
            if (remoteKeywords.isEmpty()) {
                val defaultKeywords = listOf(
                    KeywordModel(name = "Exoplanets", category = "Planetary Systems"),
                    KeywordModel(name = "Black Holes", category = "High Energy"),
                    KeywordModel(name = "Dark Matter", category = "Cosmology"),
                    KeywordModel(name = "Dark Energy", category = "Cosmology"),
                    KeywordModel(name = "Gravitational Waves", category = "High Energy"),
                    KeywordModel(name = "JWST", category = "Observatories"),
                    KeywordModel(name = "Hubble", category = "Observatories"),
                    KeywordModel(name = "SETI", category = "Planetary Systems"),
                    KeywordModel(name = "Astrobiology", category = "Planetary Systems"),
                    KeywordModel(name = "Supernovae", category = "Stellar Evolution"),
                    KeywordModel(name = "Neutron Stars", category = "Stellar Evolution"),
                    KeywordModel(name = "Pulsars", category = "Stellar Evolution"),
                    KeywordModel(name = "Quasars", category = "Extragalactic"),
                    KeywordModel(name = "Blazars", category = "Extragalactic"),
                    KeywordModel(name = "Active Galactic Nuclei", category = "Extragalactic"),
                    KeywordModel(name = "Cosmic Microwave Background", category = "Cosmology"),
                    KeywordModel(name = "Reionization", category = "Cosmology"),
                    KeywordModel(name = "First Stars", category = "Cosmology"),
                    KeywordModel(name = "Galaxy Evolution", category = "Extragalactic"),
                    KeywordModel(name = "Milky Way", category = "Galactic"),
                    KeywordModel(name = "Local Group", category = "Extragalactic"),
                    KeywordModel(name = "Star Formation", category = "Stellar Evolution"),
                    KeywordModel(name = "Protoplanetary Disks", category = "Planetary Systems"),
                    KeywordModel(name = "Solar Physics", category = "Stellar Physics"),
                    KeywordModel(name = "Space Weather", category = "Stellar Physics"),
                    KeywordModel(name = "Gamma-Ray Bursts", category = "High Energy"),
                    KeywordModel(name = "Neutrino Astronomy", category = "High Energy"),
                    KeywordModel(name = "Multi-messenger Astronomy", category = "High Energy"),
                    KeywordModel(name = "Asteroseismology", category = "Stellar Physics"),
                    KeywordModel(name = "Gaia Mission", category = "Observatories"),
                    KeywordModel(name = "Exoplanet Atmospheres", category = "Planetary Systems"),
                    KeywordModel(name = "Habitability", category = "Planetary Systems"),
                    KeywordModel(name = "Brown Dwarfs", category = "Stellar Evolution"),
                    KeywordModel(name = "White Dwarfs", category = "Stellar Evolution"),
                    KeywordModel(name = "Magnetars", category = "Stellar Evolution"),
                    KeywordModel(name = "X-ray Binaries", category = "High Energy"),
                    KeywordModel(name = "Tidal Disruption Events", category = "High Energy"),
                    KeywordModel(name = "Gravitational Lensing", category = "Cosmology"),
                    KeywordModel(name = "Large Scale Structure", category = "Cosmology"),
                    KeywordModel(name = "Baryon Acoustic Oscillations", category = "Cosmology"),
                    KeywordModel(name = "Fast Radio Bursts", category = "High Energy"),
                    KeywordModel(name = "Interstellar Medium", category = "Galactic"),
                    KeywordModel(name = "Galactic Center", category = "Galactic"),
                    KeywordModel(name = "Cosmic Rays", category = "High Energy"),
                    KeywordModel(name = "Dark Nebulae", category = "Galactic"),
                    KeywordModel(name = "Planetary Nebulae", category = "Stellar Evolution"),
                    KeywordModel(name = "Open Clusters", category = "Galactic"),
                    KeywordModel(name = "Globular Clusters", category = "Galactic"),
                    KeywordModel(name = "HII Regions", category = "Galactic"),
                    KeywordModel(name = "Molecular Clouds", category = "Galactic"),
                    KeywordModel(name = "Astrochemistry", category = "Interdisciplinary"),
                    KeywordModel(name = "Astrometry", category = "Observational Techniques"),
                    KeywordModel(name = "Photometry", category = "Observational Techniques"),
                    KeywordModel(name = "Spectroscopy", category = "Observational Techniques"),
                    KeywordModel(name = "Interferometry", category = "Observational Techniques"),
                    KeywordModel(name = "Adaptive Optics", category = "Observational Techniques"),
                    KeywordModel(name = "VLBI", category = "Observational Techniques"),
                    KeywordModel(name = "ALMA", category = "Observatories"),
                    KeywordModel(name = "VLT", category = "Observatories"),
                    KeywordModel(name = "Keck", category = "Observatories"),
                    KeywordModel(name = "LSST", category = "Observatories"),
                    KeywordModel(name = "Euclid", category = "Observatories"),
                    KeywordModel(name = "Roman Space Telescope", category = "Observatories"),
                    KeywordModel(name = "Chandra", category = "Observatories"),
                    KeywordModel(name = "XMM-Newton", category = "Observatories"),
                    KeywordModel(name = "Fermi", category = "Observatories"),
                    KeywordModel(name = "Swift", category = "Observatories"),
                    KeywordModel(name = "IceCube", category = "Observatories"),
                    KeywordModel(name = "LIGO", category = "Observatories"),
                    KeywordModel(name = "Virgo", category = "Observatories"),
                    KeywordModel(name = "KAGRA", category = "Observatories"),
                    KeywordModel(name = "LISA", category = "Observatories"),
                    KeywordModel(name = "James Webb", category = "Observatories"),
                    KeywordModel(name = "Spitzer", category = "Observatories"),
                    KeywordModel(name = "Herschel", category = "Observatories"),
                    KeywordModel(name = "Planck", category = "Observatories"),
                    KeywordModel(name = "WMAP", category = "Observatories"),
                    KeywordModel(name = "COBE", category = "Observatories"),
                    KeywordModel(name = "Rosetta", category = "Observatories"),
                    KeywordModel(name = "Cassini", category = "Observatories"),
                    KeywordModel(name = "Juno", category = "Observatories"),
                    KeywordModel(name = "Mars Exploration", category = "Planetary Systems"),
                    KeywordModel(name = "Lunar Research", category = "Planetary Systems"),
                    KeywordModel(name = "Asteroids", category = "Planetary Systems"),
                    KeywordModel(name = "Comets", category = "Planetary Systems"),
                    KeywordModel(name = "Kuiper Belt", category = "Planetary Systems"),
                    KeywordModel(name = "Oort Cloud", category = "Planetary Systems"),
                    KeywordModel(name = "Orbital Mechanics", category = "Astrodynamics"),
                    KeywordModel(name = "N-body Simulations", category = "Computational"),
                    KeywordModel(name = "Relativistic Astrophysics", category = "Theoretical"),
                    KeywordModel(name = "Plasma Astrophysics", category = "Theoretical"),
                    KeywordModel(name = "Nucleosynthesis", category = "Stellar Physics"),
                    KeywordModel(name = "Big Bang Nucleosynthesis", category = "Cosmology"),
                    KeywordModel(name = "Stellar Atmospheres", category = "Stellar Physics"),
                    KeywordModel(name = "Stellar Interiors", category = "Stellar Physics"),
                    KeywordModel(name = "Solar Neutrinos", category = "Stellar Physics"),
                    KeywordModel(name = "Helioseismology", category = "Stellar Physics"),
                    KeywordModel(name = "Exomoons", category = "Planetary Systems"),
                    KeywordModel(name = "Hot Jupiters", category = "Planetary Systems"),
                    KeywordModel(name = "Super-Earths", category = "Planetary Systems"),
                    KeywordModel(name = "Earth-like Planets", category = "Planetary Systems")
                )
                supabaseClient.postgrest["keywords"].insert(defaultKeywords)
                
                // Also update local
                keywordDao.insertKeywords(defaultKeywords.map { KeywordEntity(it.name, it.category) })
            }
        } catch (e: Exception) {
            // Ignore seeding errors
        }
    }
}
