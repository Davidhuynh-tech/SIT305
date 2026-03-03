package com.example.UnitConverter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Components
    private Spinner spinnerSource;
    private Spinner spinnerDestination;
    private EditText editInputValue;
    private Button buttonConvert;
    private TextView textResult;

    //Conversions
    private static final double USD_TO_AUD = 1.55;
    private static final double USD_TO_EUR = 0.92;
    private static final double USD_TO_JPY = 148.50;
    private static final double USD_TO_GBP = 0.78;
    private static final double MPG_TO_KML = 0.425;
    private static final double GALLON_TO_LITER = 3.785;
    private static final double NAUTICAL_MILE_TO_KM = 1.852;

    //Save the unit into array
    private static final String[][] UNIT_GROUPS = {
            {"USD", "AUD", "EUR", "JPY", "GBP"},
            {"MPG", "KM/L"},
            {"Gallon", "Liter"},
            {"NauticalMile", "KM"},
            {"Celsius", "Fahrenheit", "Kelvin"}
    };

    //Flat out into one list for the loop
    private final String[] allUnits = buildFlatUnitList();
    private static String[] buildFlatUnitList() {
        List<String> list = new ArrayList<>();
        for (String[] group : UNIT_GROUPS) {
            for (String value : group) {
                list.add(value);
            }
        }
        return list.toArray(new String[0]);
    }
    //Set up the view and components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerSource = findViewById(R.id.spinner_source);
        spinnerDestination = findViewById(R.id.spinner_destination);
        editInputValue = findViewById(R.id.edit_input_value);
        buttonConvert = findViewById(R.id.button_convert);
        textResult = findViewById(R.id.text_result);

        setupSourceSpinner();
        buttonConvert.setOnClickListener(v -> performConversion());
    }

    //Parent Spinner
    private void setupSourceSpinner() {
        ArrayAdapter<String> sourceAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allUnits);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSource.setAdapter(sourceAdapter);
        //User select an item 
        spinnerSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //Update the item 
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updateDestinationSpinner(allUnits[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    //Update child spinner list to show the sorted value
    private void updateDestinationSpinner(String sourceUnit) {
        int group = -1;
        for (int g = 0; g < UNIT_GROUPS.length; g++) {
            for (String value : UNIT_GROUPS[g]) {
                if (value.equals(sourceUnit)) {
                    group = g;
                    break;
                }
            }
            if (group != -1) break;
        }

        if (group < 0) return;

        String[] groupUnits = UNIT_GROUPS[group];
        List<String> destinations = new ArrayList<>();
        for (String value : groupUnits) {
            if (!value.equals(sourceUnit)) {
                destinations.add(value);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                destinations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestination.setAdapter(adapter);
    }

    //Conversion Logic
    private static double convert(String sourceUnit, String destinationUnit, double value) {
        String key = sourceUnit + "_to_" + destinationUnit;

        switch (key) {
            case "USD_to_AUD":
                return value * USD_TO_AUD;
            case "AUD_to_USD":
                return value / USD_TO_AUD;
            case "USD_to_EUR":
                return value * USD_TO_EUR;
            case "EUR_to_USD":
                return value / USD_TO_EUR;
            case "USD_to_JPY":
                return value * USD_TO_JPY;
            case "JPY_to_USD":
                return value / USD_TO_JPY;
            case "USD_to_GBP":
                return value * USD_TO_GBP;
            case "GBP_to_USD":
                return value / USD_TO_GBP;
            case "MPG_to_KM/L":
                return value * MPG_TO_KML;
            case "KM/L_to_MPG":
                return value / MPG_TO_KML;
            case "Gallon_to_Liter":
                return value * GALLON_TO_LITER;
            case "Liter_to_Gallon":
                return value / GALLON_TO_LITER;
            case "NauticalMile_to_KM":
                return value * NAUTICAL_MILE_TO_KM;
            case "KM_to_NauticalMile":
                return value / NAUTICAL_MILE_TO_KM;
            case "Celsius_to_Fahrenheit":
                return (value * 1.8) + 32;
            case "Fahrenheit_to_Celsius":
                return (value - 32) / 1.8;
            case "Celsius_to_Kelvin":
                return value + 273.15;
            case "Kelvin_to_Celsius":
                return value - 273.15;
            default:
                return value;
        }
    }

    //Perform Conversion
    private void performConversion() {
        String inputText = editInputValue.getText().toString().trim();
        double value = Double.parseDouble(inputText);

        String sourceUnit = spinnerSource.getSelectedItem().toString();
        String destUnit = spinnerDestination.getSelectedItem().toString();

        double result = convert(sourceUnit, destUnit, value);
        textResult.setText(result + " " + destUnit);
    }
}
