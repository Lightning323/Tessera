/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.nuklear.components;

import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.Nuklear;

/**
 *
 * @author Patron
 */
public class NumberBox extends TextBox {

    /**
     * @return the minValue
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public static double DEFAULT_MIN_VALUE = -10E300;
    public static double DEFAULT_MAX_VALUE = 10E300;

    private double minValue = DEFAULT_MIN_VALUE;
    private double maxValue = DEFAULT_MAX_VALUE;

    private final String decimalPattern; //10 decimals max per number
    private final boolean multiDecimal;
    private static final String myRegex = "\\.?0*$"; //remove trailing zeroes

    /**
     *
     * @param maxLength the maximum number of characters allowed
     * @param decimalPointLimit the maximum number of decimals allowed (set to 0
     * if you want only integer values to be allowed)
     */
    public NumberBox(int maxLength, int decimalPointLimit) {
        super(maxLength);
        if (decimalPointLimit <= 0) {
            multiDecimal = false;
            filter = NkPluginFilter.create(Nuklear::nnk_filter_decimal);
        } else {
            multiDecimal = true;
            filter = NkPluginFilter.create(Nuklear::nnk_filter_float);
        }
        decimalPattern = "%." + decimalPointLimit + "f";
        setValueAsNumber(0);
    }

    /**
     *
     * @param maxLength the maximum number of characters allowed
     */
    public NumberBox(int maxLength) {
        super(maxLength);
        filter = NkPluginFilter.create(Nuklear::nnk_filter_float);
        multiDecimal = true;
        decimalPattern = "%.10f";
        setValueAsNumber(0);
    }

    @Override
    protected void onChangeEvent() {
        setValueAsNumber(getValueAsNumber());
    }

    public double getValueAsNumber() {
        try {
            double number = Double.parseDouble(getValueAsString());
            return (number);
        } catch (NumberFormatException ex) {
            return (0);
        }
    }

    public void setValueAsNumber(double number) {
        if (number < minValue) {
            number = minValue;
        } else if (number > maxValue) {
            number = maxValue;
        }

        //Convert number to string (with a max number of decimals)
        String str = String.format(decimalPattern, number);
        if (multiDecimal) {
            str = str.replaceAll(myRegex, "");
        }
//        System.out.println("Setting: " + str);
        setValueAsString(str);
    }
}
