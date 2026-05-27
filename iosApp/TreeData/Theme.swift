//
//  Theme.swift
//  TreeData
//
//  Shared color and style definitions for the Orchard Green theme.
//  Colors reference assets in Assets.xcassets with light/dark variants.
//

import SwiftUI

// MARK: - Theme Constants

enum OrchardTheme {
    /// Forest green (#2E7D32 light, #66BB6A dark)
    static let primary = Color("OrchardPrimary")

    /// Warm brown (#8D6E63 light, #A1887F dark)
    static let secondary = Color("OrchardSecondary")

    /// Warm white / dark green-black background
    static let background = Color("OrchardBackground")

    /// White / dark olive surface for cards and forms
    static let surface = Color("OrchardSurface")

    /// Muted green-gray secondary text
    static let textSecondary = Color("OrchardTextSecondary")
}

// MARK: - Button Styles

struct OrchardPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .fontWeight(.semibold)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(OrchardTheme.primary.opacity(configuration.isPressed ? 0.8 : 1.0))
            .foregroundColor(.white)
            .cornerRadius(10)
    }
}

struct OrchardSecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .fontWeight(.medium)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(OrchardTheme.secondary.opacity(configuration.isPressed ? 0.8 : 1.0))
            .foregroundColor(.white)
            .cornerRadius(10)
    }
}

struct OrchardDestructiveButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .fontWeight(.medium)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(Color.red.opacity(configuration.isPressed ? 0.7 : 0.85))
            .foregroundColor(.white)
            .cornerRadius(10)
    }
}
