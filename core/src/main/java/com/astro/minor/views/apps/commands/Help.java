package com.astro.minor.views.apps.commands;

public class Help extends Command<String []>{
    @Override
    public String execute(String[] args) {
        this.status = CmdStatus.OK;
        return "Список команд: \n" +
            "help - список команд\n" +
            "version - версия ОС\n" +
            "time - время\n" +
            "echo TEXT - вывести текст\n" +
            "clear - очистить консоль\n" +
            "start PROG - запустить программу\n" +
            "ls - выводит список всех файлов и каталогов. ls -la выводит подробности\n" +
            "cd - изменить директорию. > cd root - зайти в root, > cd .. - выйти на директорию вверх\n" +
            "exec FILE - исполнить файл. > exec hello.txt\n" +
            "pwd - получить директорию, где я сейчас нахожусь";

    }
}
