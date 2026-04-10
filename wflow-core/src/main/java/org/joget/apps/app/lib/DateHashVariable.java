package org.joget.apps.app.lib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.springframework.context.i18n.LocaleContextHolder;

public class DateHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        return getValue(variableKey, null);
    }
    
    protected String getValue(String variableKey, String locale) {
        try {
            if (locale == null || locale.isEmpty()) {
                locale = "en_US";
            }
            
            Calendar cal = Calendar.getInstance();
            
            if (variableKey.contains("[") && variableKey.contains("]")) {
                String date = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]"));
                variableKey = variableKey.substring(0, variableKey.indexOf("["));

                if (!date.isEmpty()) {
                    try {
                        String format = "yyyy-MM-dd";
                        String timezone= null;
                        if (date.contains("|")) {
                            String[] temp = date.split("\\|");
                            format = temp[1];
                            date = temp[0];
                            if (date == null || date.isEmpty()) {
                                return null;
                            }
                            if (temp.length == 3) {
                                timezone = temp[2];
                            }
                        }
                        if (date.startsWith("{") && date.endsWith("}")) {
                            LogUtil.debug(DateHashVariable.class.getName(), "variable: " + variableKey + " contains unparsable date.");
                            return null;
                        }

                        DateFormat df = new SimpleDateFormat(format);
                        if (timezone != null && !timezone.isEmpty()) {
                            try {
                                TimeZone tz = null;
                                if ("default".equalsIgnoreCase(timezone)) {
                                    tz = TimeZone.getDefault();
                                } else {
                                    tz = TimeZone.getTimeZone(TimeZoneUtil.getTimeZoneByGMT(timezone));
                                }
                                if (tz != null) {
                                    df.setTimeZone(tz);
                                }
                            } catch (Exception er) {
                                LogUtil.error(DateHashVariable.class.getName(), er, "");
                            }
                        } else {
                            // If no timezone is specified, fallback to profile or system timezone to align parsing with formatting
                            df.setTimeZone(LocaleContextHolder.getTimeZone());
                        }
                        Date result =  df.parse(date);
                        cal.setTime(result);
                    } catch (Exception e) {
                        LogUtil.error(DateHashVariable.class.getName(), e, "");
                    }
                }
            }

            int field = -1;
            if (variableKey.contains("YEAR")) {
                field = Calendar.YEAR;
                variableKey = variableKey.replace("YEAR", "");
            } else if (variableKey.contains("MONTH")) {
                field = Calendar.MONTH;
                variableKey = variableKey.replace("MONTH", "");
            } else if (variableKey.contains("DAY")) {
                field = Calendar.DATE;
                variableKey = variableKey.replace("DAY", "");
            }

            if (field != -1) {
                String amount = variableKey.substring(0, variableKey.indexOf("."));
                variableKey = variableKey.replace(amount + ".", "");

                amount = amount.replace("+", "");

                cal.add(field, Integer.parseInt(amount));
            }
            
            String timezone = null;
            if (variableKey.contains("|")) {
                String[] temp = variableKey.split("\\|");
                if (temp.length == 2) {
                    variableKey = temp[0];
                    timezone = temp[1];
                    if ("default".equalsIgnoreCase(timezone)) {
                        timezone = TimeZone.getDefault().getID();
                    }
                }
            }

            String dateOutput = TimeZoneUtil.convertToTimeZoneWithLocale(cal.getTime(), timezone, variableKey, locale);
            if ("th".equals(LocaleContextHolder.getLocale().getLanguage()) && "TH".equals(LocaleContextHolder.getLocale().getCountry())) {
                dateOutput = convertToBuddhistFormat(dateOutput, variableKey);
            }
            return dateOutput;

        } catch (IllegalArgumentException iae) {
            return new Date().toString();
        } catch (Exception ex) {
            LogUtil.error(DateHashVariable.class.getName(), ex, "");
            return null;
        }
    }

    protected String convertToBuddhistFormat (String value, String format) {
        DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(format)
                .toFormatter(Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH)
                .withChronology(ThaiBuddhistChronology.INSTANCE);

        // Date only
        try {
            LocalDate localDate = LocalDate.parse(value, inputFormatter);
            ThaiBuddhistDate thaiDate = ThaiBuddhistDate.from(localDate);
            return outputFormatter.format(thaiDate);
        } catch (Exception e) {}

        // Date & Time
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(value, inputFormatter);
            ChronoZonedDateTime<ThaiBuddhistDate> thaiDateTime = ThaiBuddhistChronology.INSTANCE.zonedDateTime(localDateTime.atZone(java.time.ZoneId.systemDefault()));
            return outputFormatter.format(thaiDateTime);
        } catch (Exception e) {}

        // Time only or unformatted
        return value;
    }

    public String getName() {
        return "Date Hash Variable";
    }

    public String getPrefix() {
        return "date";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Date Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("date.FORMAT");
        syntax.add("date.FORMAT|TIMEZONE");
        syntax.add("date.DAY+/-INTEGER.FORMAT");
        syntax.add("date.MONTH+/-INTEGER.FORMAT");
        syntax.add("date.YEAR+/-INTEGER.FORMAT");
        syntax.add("date.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("date.DAY+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("date.MONTH+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("date.YEAR+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        return syntax;
    }
    
    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/dateHashVariable.json", null, true, null);
    }
}
