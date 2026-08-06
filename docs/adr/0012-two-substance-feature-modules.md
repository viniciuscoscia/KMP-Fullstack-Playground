# ADR 0012: Add exactly two Substance Atlas Gradle modules

Status: accepted

Add a multiplatform contract module and a multiplatform client module. Keep presentation in `:app:shared`, native SwiftUI in the Xcode host, and server capabilities in `:server`. This preserves a small learning repository while giving wire contracts and transport adapters independent ownership.
