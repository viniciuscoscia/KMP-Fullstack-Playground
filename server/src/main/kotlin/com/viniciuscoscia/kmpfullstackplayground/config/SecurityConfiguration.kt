package com.viniciuscoscia.kmpfullstackplayground.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Configuration
class SecurityConfiguration {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        localTokenFilter: LocalTokenAuthenticationFilter,
    ): SecurityFilterChain = http
        .csrf { it.disable() }
        .cors(withDefaults())
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { requests ->
            requests
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/research-jobs").authenticated()
                .requestMatchers("/api/v1/drafts/**", "/mcp", "/mcp/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                .anyRequest().denyAll()
        }
        .exceptionHandling { errors ->
            errors.authenticationEntryPoint { _, response, _ ->
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.contentType = "application/problem+json"
                response.writer.write(
                    """{"type":"https://substance-atlas.local/problems/401","title":"Authentication required","status":401}""",
                )
            }
        }
        .addFilterBefore(localTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("http://127.0.0.1:*", "http://localhost:*")
            allowedMethods = listOf("GET", "POST", "OPTIONS")
            allowedHeaders = listOf(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "Idempotency-Key", HttpHeaders.ACCEPT_LANGUAGE)
            exposedHeaders = listOf(HttpHeaders.CONTENT_DISPOSITION)
            allowCredentials = false
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().also { it.registerCorsConfiguration("/**", configuration) }
    }
}

@Component
class LocalTokenAuthenticationFilter(
    @Value("\${substance-atlas.security.admin-token:}") private val configuredToken: String,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val suppliedToken = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
        if (configuredToken.isNotBlank() && suppliedToken != null && constantTimeEquals(configuredToken, suppliedToken)) {
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                "local-admin",
                null,
                listOf(SimpleGrantedAuthority("ROLE_LOCAL_ADMIN")),
            )
        }
        filterChain.doFilter(request, response)
    }

    private fun constantTimeEquals(expected: String, actual: String): Boolean = MessageDigest.isEqual(
        expected.toByteArray(StandardCharsets.UTF_8),
        actual.toByteArray(StandardCharsets.UTF_8),
    )
}
