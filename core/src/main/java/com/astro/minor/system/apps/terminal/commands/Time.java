package com.astro.minor.system.apps.terminal.commands;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Time extends Command<String []>{
    @Override
    public String execute(String[] args) {
        this.status = CmdStatus.OK;

        Calendar cal1998 = new GregorianCalendar(1998, Calendar.SEPTEMBER, 28);
        Calendar now = Calendar.getInstance();
        cal1998.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal1998.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        cal1998.set(Calendar.SECOND, now.get(Calendar.SECOND));

        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        return "Сейчас: " + formatter.format(cal1998.getTime());
    }
}

