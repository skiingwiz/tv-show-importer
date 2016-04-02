//import ch.qos.logback.classic.Level;

scan()

//String prop = System.getProperty("tvshow.logging.console")
//Level consoleLevel = prop == null ? INFO : Level.valueOf(prop);

appender("FILE", FileAppender) {
    file = "tvimporter.log"
    append = true
    
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "tvimporter-%i.log"
        minIndex = 1
        maxIndex = 3
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = "10MB"
    }
    encoder(PatternLayoutEncoder) {
      pattern = "%level %d{HH:mm:ss.SSS} %logger - %msg%n"
    }
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

//logger("com.foo", INFO, ["CONSOLE"])

root(INFO, ["CONSOLE", "FILE"])
