package com.astro.minor.system.apps.terminal.commands;

import java.io.File;

public class Psyche extends Command<String[]> {

    private static final String WINDOWS_VHD_PATH = "C:\\PsycheChip.vhd";
    private static final String WINDOWS_MOUNT_POINT = "P:\\";
    private static final String LINUX_IMG_PATH = "/tmp/psyche.img";
    private static final String LINUX_MOUNT_POINT = "/mnt/psyche";
    private static final String MACOS_DMG_PATH = "/tmp/psyche.dmg";
    private static final String MACOS_MOUNT_POINT = "/Volumes/PSYCHE_CHIP";

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            this.status = CmdStatus.ERROR;
            return "Использование: psyche status";
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("status")) {
            return checkPsycheChip();
        } else {
            this.status = CmdStatus.ERROR;
            return "Неизвестная команда. Используйте: psyche status";
        }
    }

    private String checkPsycheChip() {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean detected = false;
        String mountPoint = "";
        String devicePath = "";

        if (osName.contains("win")) {
            detected = checkWindows();
            mountPoint = WINDOWS_MOUNT_POINT;
            devicePath = WINDOWS_VHD_PATH;
        } else if (osName.contains("nux")) {
            detected = checkLinux();
            mountPoint = LINUX_MOUNT_POINT;
            devicePath = LINUX_IMG_PATH;
        } else if (osName.contains("mac")) {
            detected = checkMacOS();
            mountPoint = MACOS_MOUNT_POINT;
            devicePath = MACOS_DMG_PATH;
        }

        StringBuilder result = new StringBuilder();
        result.append("╔═══════════════════════════════════════════════╗\n");
        result.append("║       PSYCHE CHIP - СТАТУС СИСТЕМЫ           ║\n");
        result.append("║   Protocol 7 - Система доступа WIRED         ║\n");
        result.append("╚═══════════════════════════════════════════════╝\n");
        result.append("\n");

        if (detected) {
            this.status = CmdStatus.OK;
            result.append("► СТАТУС: [ОБНАРУЖЕН]\n");
            result.append("\n");
            result.append("╔═══════════════════════════════════════════════╗\n");
            result.append("║  Соединение с WIRED установлено.             ║\n");
            result.append("║  \"Настоящий день, настоящее время...\"         ║\n");
            result.append("╚═══════════════════════════════════════════════╝\n");
        } else {
            this.status = CmdStatus.ERROR;
            result.append("► СТАТУС: [НЕ ОБНАРУЖЕН]\n");
            result.append("► ОЖИДАЕМЫЙ ПУТЬ: ").append(devicePath).append("\n");
            result.append("► ТОЧКА МОНТИРОВАНИЯ: ").append(mountPoint).append("\n");
            result.append("\n");
            result.append("╔═══════════════════════════════════════════════╗\n");
            result.append("║  PSYCHE CHIP не найден.                      ║\n");
            result.append("║  Доступ к WIRED невозможен.                  ║\n");
            result.append("╚═══════════════════════════════════════════════╝\n");
        }

        return result.toString();
    }

    private boolean checkWindows() {
        File vhdFile = new File(WINDOWS_VHD_PATH);
        File mountPoint = new File(WINDOWS_MOUNT_POINT);
        return vhdFile.exists() || mountPoint.exists();
    }

    private boolean checkLinux() {
        File imgFile = new File(LINUX_IMG_PATH);
        File mountPoint = new File(LINUX_MOUNT_POINT);
        return imgFile.exists() || mountPoint.exists();
    }

    private boolean checkMacOS() {
        File dmgFile = new File(MACOS_DMG_PATH);
        File mountPoint = new File(MACOS_MOUNT_POINT);
        return dmgFile.exists() || mountPoint.exists();
    }

    public boolean isPsycheChipDetected() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return checkWindows();
        } else if (osName.contains("nux")) {
            return checkLinux();
        } else if (osName.contains("mac")) {
            return checkMacOS();
        }

        return false;
    }
}

