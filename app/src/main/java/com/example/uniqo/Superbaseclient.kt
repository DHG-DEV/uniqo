package com.example.uniqo

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://skpyrgqtyfypqtoinvpw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrcHlyZ3F0eWZ5cHF0b2ludnB3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1MzMxNzQsImV4cCI6MjEwMzEwOTE3NH0.XbBn44BZDDcLNNO5hGa1ttDIlNiOkT-rZTN5PLuuT_Q"
    ) {
        install(Postgrest)
        install(Auth) {
            alwaysAutoRefresh = false   // custom backend issues tokens, not GoTrue — no real refresh token exists to refresh with
            autoLoadFromStorage = true  // still load the stored access token on startup
        }
        install(Storage)
        install(Realtime)
    }
}