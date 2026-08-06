# ADR 0015: Substance Atlas uses native SwiftUI on Apple platforms

Status: accepted

Android, Desktop, and Web share adaptive Compose presentation. iOS and iPadOS use SwiftUI, Swift Charts, SF Symbols, NavigationStack, and NavigationSplitView over a Swift-friendly shared repository facade. Existing Compose UIKit interop remains available for future DSA routes.
