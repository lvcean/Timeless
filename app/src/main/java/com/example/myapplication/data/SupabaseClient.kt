package com.example.myapplication.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth

object SupabaseModule {
    private const val SUPABASE_URL = "https://mwmmknlbeokrldsybsje.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im13bW1rbmxiZW9rcmxkc3lic2plIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxODE1MTEsImV4cCI6MjA4MDc1NzUxMX0.HxoClrQpexO1nvXTNJ5OvYhHyWCKl6S7x1sEMHZjMCg"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        // install(Postgrest) // We use Retrofit for data operations now
    }
}
