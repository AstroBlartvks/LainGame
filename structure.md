# Структура проекта 

https://drive.google.com/drive/folders/1zC0i312u6JIiXp6MNOxC4sDi2DrNQyd6?usp=sharing (здесь была моделька ИИ (дообученный и квантизированный квен), но я не хотел 5 Гб держать на диске)

## Общая структура

```
Lain/
├── assets/                          # Ресурсы (текстуры, звуки, шейдеры)
├── core/                            # Основной модуль игры
│   └── src/main/java/com/astro/minor/
│       ├── core/                    # Ядро приложения
│       │   ├── config/
│       │   │   └── AppConfig.java           # Конфигурация приложения (константы)
│       │   └── ServiceLocator.java          # Service Locator для доступа к сервисам
│       │
│       ├── system/                  # Системные компоненты OS
│       │   ├── desktop/
│       │   │   ├── DesktopSystemOS.java     # Главный рабочий стол
│       │   │   └── MenuBar.java             # Панель меню (taskbar)
│       │   │
│       │   ├── graphics/
│       │   │   ├── CRTShaderSystemVHS.java  # VHS CRT эффект
│       │   │   └── StartupVideoPlayer.java  # Видеоплеер загрузки
│       │   │
│       │   └── apps/                # Приложения OS
│       │       ├── base/
│       │       │   ├── Application.java         # Базовый класс приложения
│       │       │   ├── AppExecutor.java         # Менеджер приложений
│       │       │   └── IApplication.java        # Интерфейс приложения
│       │       │
│       │       ├── terminal/
│       │       │   ├── Console.java             # Консоль/терминал
│       │       │   └── commands/                # Команды терминала
│       │       │       ├── Command.java         # Базовый класс команды
│       │       │       ├── Cd.java             # cd - смена директории
│       │       │       ├── Clear.java          # clear - очистка
│       │       │       ├── Connect.java        # connect - подключение к хостам
│       │       │       ├── Crt.java            # crt - управление CRT эффектом
│       │       │       ├── Echo.java           # echo
│       │       │       ├── Edit.java           # edit - редактирование файлов
│       │       │       ├── Exec.java           # exec - выполнение файлов
│       │       │       ├── Help.java           # help
│       │       │       ├── Lain.java           # lain - пасхалка
│       │       │       ├── Love.java           # love - пасхалка
│       │       │       ├── Ls.java             # ls - список файлов
│       │       │       ├── Memex.java          # memex - открыть редактор
│       │       │       ├── Mkdir.java          # mkdir - создать папку
│       │       │       ├── Nmap.java           # nmap - сканирование сети
│       │       │       ├── Psyche.java         # psyche - психологический статус
│       │       │       ├── Pwd.java            # pwd - текущая директория
│       │       │       ├── Signal.java         # signal - работа с сигналами
│       │       │       ├── Start.java          # start - запуск приложений
│       │       │       ├── Time.java           # time - текущее время
│       │       │       ├── Touch.java          # touch - создать файл
│       │       │       ├── Version.java        # version - версия OS
│       │       │       └── Watcher.java        # watcher - система наблюдателя
│       │       │
│       │       ├── browser/
│       │       │   └── Browser.java             # Веб-браузер (WPML)
│       │       │
│       │       ├── explorer/
│       │       │   └── FileExplorer.java        # Файловый менеджер
│       │       │
│       │       ├── media/
│       │       │   ├── MusicPlayer.java         # Аудиоплеер
│       │       │   └── MyVideoPlayer.java       # Видеоплеер
│       │       │
│       │       └── editor/
│       │           └── MemexEditor.java          # Текстовый редактор
│       │
│       ├── filesystem/              # Файловая система (было fileOS)
│       │   ├── core/
│       │   │   ├── FileSystemManager.java   # Менеджер файловой системы
│       │   │   └── FileSystemContext.java   # Контекст (текущая директория)
│       │   │
│       │   ├── items/
│       │   │   ├── FileSystemItem.java      # Базовый класс элемента ФС
│       │   │   ├── Directory.java           # Директория
│       │   │   ├── MyFile.java              # Файл
│       │   │   └── FileType.java            # Типы файлов (enum)
│       │   │
│       │   ├── network/
│       │   │   ├── NetworkManager.java      # Менеджер сети
│       │   │   └── NetworkHost.java         # Хост в сети
│       │   │
│       │   └── observers/
│       │       └── FileSystemObserver.java  # Observer для изменений ФС
│       │
│       ├── wired/                   # WiredPages - браузерный движок
│       │   ├── core/
│       │   │   ├── WiredDocument.java       # Документ WPML
│       │   │   ├── WiredDocumentParser.java # Парсер документов
│       │   │   ├── WiredPageRenderer.java   # Рендерер страниц
│       │   │   ├── ElementParser.java       # Интерфейс парсера элементов
│       │   │   ├── DefaultElementParser.java
│       │   │   ├── WpmlElementParser.java
│       │   │   ├── AttributeParser.java     # Парсер атрибутов
│       │   │   ├── ParserFactory.java       # Фабрика парсеров
│       │   │   └── ParseContext.java        # Контекст парсинга
│       │   │
│       │   ├── elements/
│       │   │   ├── WiredElement.java        # Базовый элемент WPML
│       │   │   ├── LayoutBox.java           # Layout контейнер
│       │   │   └── LinkInfo.java            # Информация о ссылке
│       │   │
│       │   ├── events/
│       │   │   ├── WiredPageEvent.java      # Базовый класс события
│       │   │   ├── WiredPageEventListener.java
│       │   │   ├── OnClickEvent.java        # Событие клика
│       │   │   └── SubmitEvent.java         # Событие отправки формы
│       │   │
│       │   └── tags/
│       │       └── WiredPageMLElement.java   # WPML элемент
│       │
│       ├── signal/                  # Система сигналов (психологическая)
│       │   ├── SignalGenerator.java         # Генератор сигналов
│       │   ├── SignalData.java              # Данные сигнала
│       │   ├── SignalType.java              # Типы сигналов
│       │   ├── SignalSaver.java             # Сохранение сигналов
│       │   ├── ProcessWatcher.java          # Наблюдатель за процессами
│       │   └── CognitiveStabilitySystem.java # Система когнитивной стабильности
│       │
│       ├── audio/                   # Аудиосистема
│       │   ├── ComputerAmbience.java        # Эмбиент звуки компьютера
│       │   └── WatcherSystem.java           # Система наблюдателя (аудио события)
│       │
│       ├── ui/                      # UI компоненты
│       │   ├── UIComponent.java             # Интерфейс UI компонента
│       │   ├── BaseUIComponent.java         # Базовый UI компонент
│       │   ├── Button.java                  # Кнопка
│       │   ├── Label.java                   # Текстовая метка
│       │   ├── TextField.java               # Текстовое поле
│       │   ├── TextArea.java                # Многострочное текстовое поле
│       │   ├── Slider.java                  # Слайдер
│       │   ├── Panel.java                   # Панель
│       │   ├── VBox.java                    # Вертикальный контейнер
│       │   ├── HBox.java                    # Горизонтальный контейнер
│       │   ├── Grid.java                    # Сетка
│       │   └── FontManager.java             # Менеджер шрифтов
│       │
│       └── StarField.java           # 🎮 Главный класс игры
│
├── lwjgl3/                          # LWJGL3 launcher
│   └── src/main/java/com/astro/minor/lwjgl3/
│       ├── Lwjgl3Launcher.java
│       └── StartupHelper.java
│
├── build.gradle                     # Gradle конфигурация
└── settings.gradle
```

## Ключевые в структуре

1. **core/config/** - Централизованная конфигурация
2. **core/ServiceLocator** - Паттерн Service Locator
3. **system/** - Системные компоненты (desktop, graphics, apps)
4. **filesystem/** - Переименовано из fileOS
5. **wired/** - Переименовано из WiredPages
6. **audio/** - Переименовано из sounds

---

1. **Приложения** разделены по категориям:
   - base/ - базовые классы
   - terminal/ - консоль + команды
   - browser/ - веб-браузер
   - explorer/ - файловый менеджер
   - media/ - мультимедиа плееры
   - editor/ - текстовый редактор

2. **Файловая система** структурирована:
   - core/ - основная логика
   - items/ - элементы ФС
   - network/ - сетевые компоненты
   - observers/ - паттерн Observer

3. **Wired движок** разделен:
   - core/ - парсинг и рендеринг
   - elements/ - элементы страниц
   - events/ - события
   - tags/ - теги WPML

### Service Locator
```java
ServiceLocator.getAppExecutor()
ServiceLocator.getFileSystemManager()
ServiceLocator.getUiCamera()
```

### Singleton
- FileSystemManager
- SignalGenerator
- ProcessWatcher
- CognitiveStabilitySystem
- WatcherSystem
- CRTShaderSystemVHS

### Observer
- FileSystemObserver
- WatcherEventListener

### Command Pattern
- Все команды в terminal/commands/

### Factory
- ParserFactory для создания парсеров

## Конфигурация (AppConfig)

```java
public static final String OS_NAME = "Copland OS";
public static final String VERSION = "Version 1.0.0";
public static final boolean DEBUG_MODE = false;
public static final int DEFAULT_WINDOW_WIDTH = 1600;
public static final int DEFAULT_WINDOW_HEIGHT = 900;
public static final float DEFAULT_SYSTEM_LOAD = 0.2f;
public static final float HIGH_SYSTEM_LOAD = 0.8f;
```

## Зависимости

### Основные
- libGDX (графический движок)
- FreeType (шрифты)
- JavaCV + FFmpeg (видео)

## Точки входа

1. **Lwjgl3Launcher** → создает окно
2. **StarField.create()** → инициализация игры
3. **StarField.initializeGame()** → загрузка систем
4. **StartupVideoPlayer** (опционально) → стартовое видео
5. **DesktopSystemOS** → рабочий стол
6. **AppExecutor** → управление приложениями
