# ADR 0013: Use a Spring Modulith modular monolith

Status: accepted

Replace the minimal Ktor server with one Kotlin/Spring Boot process. Package-by-feature modules `catalog`, `research`, `reporting`, and `assistant` expose named interfaces and are verified with Spring Modulith. MVC is the HTTP delivery adapter; application services and ports keep domain behavior independent from JPA and external sources.
