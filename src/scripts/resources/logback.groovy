scan()

appender("FILE", RollingFileAppender) {
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
      pattern = "%level %d{dd-MM-yyyy HH:mm:ss.SSS} %logger - %msg%n"
    }
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

root(INFO, ["CONSOLE", "FILE"])