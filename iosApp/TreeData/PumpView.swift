//
//  PumpView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import shared

struct PumpView: View {
    private let pumpViewModel = ViewModelProvider.shared.pumpViewModel

    @State private var pumps: [Pump] = []
    @State private var selectedPump: Pump? = nil

    // Form fields
    @State private var type: String = ""
    @State private var horsepower: String = ""
    @State private var phase: String = ""
    @State private var flowRate: String = ""
    @State private var flowRateUnit: FlowRateUnit = .gallonsperhour

    private let flowRateUnits: [FlowRateUnit] = [.gallonsperhour, .gallonsperminute]

    var body: some View {
        Form {
            Section(header: Text("Existing Pumps")) {
                    Picker("Select a Pump", selection: $selectedPump) {
                        Text("New Pump").tag(nil as Pump?)
                        ForEach(pumps, id: \.id) { pump in
                            Text(pump.type).tag(pump as Pump?)
                        }
                    }
                    .onChange(of: selectedPump) { newValue in
                        populateFields(from: newValue)
                    }
                }

                Section(header: Text("Pump Details")) {
                    TextField("Type", text: $type)
                    TextField("Horsepower", text: $horsepower)
                        .keyboardType(.decimalPad)
                    TextField("Phase", text: $phase)
                        .keyboardType(.numberPad)
                }

                Section(header: Text("Flow Rate")) {
                    HStack {
                        TextField("Flow Rate", text: $flowRate)
                            .keyboardType(.decimalPad)
                        Picker("Unit", selection: $flowRateUnit) {
                            ForEach(flowRateUnits, id: \.self) { unit in
                                Text(unit.description()).tag(unit)
                            }
                        }
                        .pickerStyle(.menu)
                    }
                }

                Section {
                    HStack {
                        Button("Save") { savePump() }
                            .buttonStyle(.borderedProminent)
                            .tint(.orchardPrimary)

                        Spacer()

                        Button("New") { resetForm() }
                            .buttonStyle(.bordered)

                        Spacer()

                        Button("Delete", role: .destructive) { deletePump() }
                            .buttonStyle(.bordered)
                            .disabled(selectedPump == nil)
                    }
                }
            }
        .navigationTitle("Pumps")
        .task {
            observeData()
        }
    }

    private func observeData() {
        let flow = pumpViewModel.pumps as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.pumps = (list as? [Pump]) ?? []
            }
        }
    }

    private func populateFields(from pump: Pump?) {
        if let p = pump {
            type = p.type
            horsepower = String(p.horsepower)
            phase = String(p.phase)
            flowRate = String(p.flowRate)
            flowRateUnit = p.flowRateUnit
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedPump = nil
        type = ""
        horsepower = ""
        phase = ""
        flowRate = ""
        flowRateUnit = .gallonsperhour
    }

    private func savePump() {
        guard !type.isEmpty else { return }

        let pump = Pump(
            id: selectedPump?.id ?? 0,
            type: type,
            horsepower: Double(horsepower) ?? 0.0,
            phase: Int32(phase) ?? 1,
            flowRate: Double(flowRate) ?? 0.0,
            flowRateUnit: flowRateUnit
        )

        if pump.id > 0 {
            pumpViewModel.updatePump(pump: pump)
        } else {
            pumpViewModel.addPump(pump: pump)
        }
        resetForm()
    }

    private func deletePump() {
        if let pump = selectedPump {
            pumpViewModel.deletePump(pump: pump)
            resetForm()
        }
    }
}
