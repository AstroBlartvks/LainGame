package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.system.graphics.CRTShaderSystemVHS;

public class Crt extends Command<String[]> {

    @Override
    public <T> T execute(String[] args) {
        this.status = CmdStatus.OK;

        String result;

        if (args.length == 0) {
            result = getStatusInfo();
        } else {
            String subcommand = args[0].toLowerCase();

            if (args.length >= 2 && subcommand.equals("color")) {
                String colorCmd = args[1].toLowerCase();
                switch (colorCmd) {
                    case "on":
                        CRTShaderSystemVHS.getInstance().setColorCorrectionEnabled(true);
                        result = "CRT цветокоррекция включена";
                        break;
                    case "off":
                        CRTShaderSystemVHS.getInstance().setColorCorrectionEnabled(false);
                        result = "CRT цветокоррекция выключена";
                        break;
                    default:
                        result = "Неизвестная команда. Используйте: crt color on/off";
                        break;
                }
            } else if (args.length >= 2 && subcommand.equals("vhs")) {
                String vhsCmd = args[1].toLowerCase();
                switch (vhsCmd) {
                    case "on":
                        CRTShaderSystemVHS.getInstance().setVhsEnabled(true);
                        result = "VHS Horror режим включён";
                        break;
                    case "off":
                        CRTShaderSystemVHS.getInstance().setVhsEnabled(false);
                        result = "VHS Horror режим выключен";
                        break;
                    case "low":
                        CRTShaderSystemVHS.getInstance().setVhsIntensity(0.4f);
                        result = "VHS интенсивность: НИЗКАЯ";
                        break;
                    case "medium":
                        CRTShaderSystemVHS.getInstance().setVhsIntensity(0.7f);
                        result = "VHS интенсивность: СРЕДНЯЯ";
                        break;
                    case "high":
                        CRTShaderSystemVHS.getInstance().setVhsIntensity(1.0f);
                        result = "VHS интенсивность: ВЫСОКАЯ";
                        break;
                    default:
                        result = "Используйте: crt vhs on/off/low/medium/high";
                        break;
                }
            } else {
                switch (subcommand) {
                    case "on":
                        CRTShaderSystemVHS.getInstance().setEnabled(true);
                        result = "CRT эффекты включены";
                        break;

                    case "off":
                        CRTShaderSystemVHS.getInstance().setEnabled(false);
                        result = "CRT эффекты выключены";
                        break;

                    case "status":
                    case "-s":
                        result = getStatusInfo();
                        break;

                    case "help":
                    case "-h":
                        result = getHelp();
                        break;

                    default:
                        result = "Неизвестная команда. Используйте 'crt help'";
                        break;
                }
            }
        }

        return (T) result;
    }

    private String getStatusInfo() {
        CRTShaderSystemVHS crt = CRTShaderSystemVHS.getInstance();
        StringBuilder sb = new StringBuilder();

        sb.append("╔═══════════════════════════════════════════╗\n");
        sb.append("║         CRT SHADER SYSTEM STATUS          ║\n");
        sb.append("╠═══════════════════════════════════════════╣\n");
        sb.append("║                                           ║\n");

        String effectsStatus = crt.isEnabled() ? "[ВКЛЮЧЕНЫ]" : "[ВЫКЛЮЧЕНЫ]";
        String colorStatus = crt.isColorCorrectionEnabled() ? "[ВКЛЮЧЕНА]" : "[ВЫКЛЮЧЕНА]";
        String vhsStatus = crt.isVhsEnabled() ? "[ENABLED]" : "[DISABLED]";

        sb.append("║  CRT эффекты:        ").append(String.format("%-20s", effectsStatus)).append("║\n");
        sb.append("║  Цветокоррекция:     ").append(String.format("%-20s", colorStatus)).append("║\n");
        sb.append("║  VHS Horror:         ").append(String.format("%-20s", vhsStatus)).append("║\n");
        sb.append("║                                           ║\n");

        if (crt.isEnabled()) {
            sb.append("║  Активные эффекты:                        ║\n");
            sb.append("║    • Scanlines       ✓                    ║\n");
            sb.append("║    • Vignette        ✓                    ║\n");
            sb.append("║    • Bloom/Glow      ✓                    ║\n");
            sb.append("║    • Curvature       ✓                    ║\n");
            sb.append("║    • Chromatic Aberr ✓                    ║\n");
            sb.append("║    • Flicker         ✓                    ║\n");
            sb.append("║    • Shadow Mask     ✓                    ║\n");
            sb.append("║                                           ║\n");
            sb.append("║  Динамические реакции:                    ║\n");
            sb.append("║    • Watcher Events  [ACTIVE]             ║\n");
            sb.append("║    • System Load     [MONITORING]         ║\n");
        }

        sb.append("║                                           ║\n");
        sb.append("╚═══════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append("Используйте 'crt help' для справки");

        return sb.toString();
    }

    private String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("CRT SHADER SYSTEM - Эмуляция ЭЛТ-монитора 90-х\n\n");
        sb.append("Использование: crt [команда]\n\n");
        sb.append("Команды:\n");
        sb.append("  on              Включить CRT эффекты\n");
        sb.append("  off             Выключить CRT эффекты\n");
        sb.append("  status, -s      Показать статус системы\n");
        sb.append("  color on        Включить цветокоррекцию\n");
        sb.append("  color off       Выключить цветокоррекцию\n");
        sb.append("  vhs on/off      Включить VHS Horror режим\n");
        sb.append("  vhs low/medium/high  Интенсивность VHS\n");
        sb.append("  help, -h        Показать эту справку\n\n");
        sb.append("Эффекты:\n");
        sb.append("  • Scanlines     - Горизонтальные линии развёртки\n");
        sb.append("  • Vignette      - Затемнение по краям экрана\n");
        sb.append("  • Bloom/Glow    - Свечение ярких участков\n");
        sb.append("  • Curvature     - Кривизна экрана кинескопа\n");
        sb.append("  • Chromatic     - Цветовое расслоение\n");
        sb.append("  • Flicker       - Лёгкое мерцание\n");
        sb.append("  • Shadow Mask   - Зернистость люминофора\n\n");
        sb.append("Динамика:\n");
        sb.append("  • При событиях Watcher усиливается цветовое расслоение\n");
        sb.append("  • При высокой нагрузке усиливается мерцание\n\n");
        sb.append("Примеры:\n");
        sb.append("  crt on            # Включить все эффекты\n");
        sb.append("  crt off           # Выключить\n");
        sb.append("  crt vhs on        # VHS Horror \n");
        sb.append("  crt vhs high      # УЖАСАЮЩИЙ режим \n");
        sb.append("  crt color off     # Только эффекты\n\n");
        sb.append("Present day... Present time... через экран CRT.");

        return sb.toString();
    }
}

