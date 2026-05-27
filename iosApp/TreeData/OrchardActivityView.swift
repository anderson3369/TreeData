//
//  OrchardActivityView.swift
//  TreeData
//
//  Created by Steven Anderson on 5/3/26.
//

import SwiftUI
import shared

struct OrchardActivityView: View {
    @State private var selectedTab = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                Picker("", selection: $selectedTab) {
                    Text("Activities").tag(0)
                    Text("Report").tag(1)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.top, 8)

                if selectedTab == 0 {
                    OrchardActivityFormView()
                } else {
                    OrchardActivityReportView()
                }
            }
            .navigationTitle("Orchard Activities")
        }
    }
}

// MARK: - Orchard Activity Report

struct OrchardActivityReportView: View {
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var orchards: [OrchardWithFarm] = []
    @State private var allActivities: [OrchardActivity] = []
    @State private var selectedOrchardId: Int64 = 0
    @State private var startDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 1, day: 1))!
    @State private var endDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 12, day: 31))!
    @State private var hasGenerated = false

    private var filteredActivities: [OrchardActivity] {
        guard selectedOrchardId != 0 else { return [] }
        let endOfDay = Calendar.current.date(bySettingHour: 23, minute: 59, second: 59, of: endDate) ?? endDate
        return allActivities.filter { activity in
            guard activity.orchardId == selectedOrchardId else { return false }
            let activityDate = activity.activityStart.toSwiftDate()
            return activityDate >= startDate && activityDate <= endOfDay
        }
    }

    private var orchardName: String {
        orchards.first(where: { $0.orchard.id == selectedOrchardId })?.orchard.crop ?? ""
    }

    var body: some View {
        Form {
            Section(header: Text("Report Filters")) {
                Picker("Orchard", selection: $selectedOrchardId) {
                    Text("Select an Orchard").tag(Int64(0))
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                    }
                }

                DatePicker("From", selection: $startDate, displayedComponents: .date)
                DatePicker("To", selection: $endDate, displayedComponents: .date)

                Button("Generate Report") {
                    hasGenerated = true
                }
                .buttonStyle(.borderedProminent)
                .tint(.orchardPrimary)
            }

            if hasGenerated && !filteredActivities.isEmpty {
                Section(header: Text(orchardName)
                    .foregroundColor(.white)
                    .font(.subheadline.weight(.bold))
                ) {
                    ForEach(filteredActivities, id: \.id) { activity in
                        OrchardActivityReportRow(activity: activity)
                    }
                }
                .listRowBackground(Color.orchardPrimary.opacity(0.8))
            } else if hasGenerated {
                Section {
                    Text("No orchard activities found for the selected filters.")
                        .foregroundColor(.gray)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func observeData() {
        let orchardsFlow = orchardViewModel.orchardsWithFarm as! CommonStateFlow<NSArray>
        orchardsFlow.subscribe { list in
            DispatchQueue.main.async {
                let oList = (list as? [OrchardWithFarm]) ?? []
                self.orchards = oList
                if selectedOrchardId == 0, let first = oList.first {
                    selectedOrchardId = first.orchard.id
                }
            }
        }

        let activitiesFlow = orchardViewModel.orchardActivities as! CommonStateFlow<NSArray>
        activitiesFlow.subscribe { list in
            DispatchQueue.main.async {
                self.allActivities = (list as? [OrchardActivity]) ?? []
            }
        }
    }
}

struct OrchardActivityReportRow: View {
    let activity: OrchardActivity

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(activity.activity)
                .fontWeight(.bold)
            if !activity.notes.isEmpty {
                Text(activity.notes)
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            let startStr = activity.activityStart.toSwiftDate().formatted(date: .abbreviated, time: .shortened)
            let stopStr = activity.activityStop.toSwiftDate().formatted(date: .abbreviated, time: .shortened)
            Text("Duration: \(startStr) to \(stopStr)")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 2)
    }
}
