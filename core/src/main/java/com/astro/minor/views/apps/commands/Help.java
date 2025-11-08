package com.astro.minor.views.apps.commands;

public class Help extends Command{
    @Override
    public String execute(String[] args) {
        this.status = CmdStatus.OK;
        return "Список команд: \n" +
            "help - список команд\n" +
            "version - версия ОС\n" +
            "time - время\n" +
            "echo TEXT - вывести текст\n" +
            "clear - очистить консоль";
    }
}
