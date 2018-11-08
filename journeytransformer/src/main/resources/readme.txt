To update the database to match the change logs for liquibase:
mvn liquibase:update


To create change logs from a database already created, with structure and data:
mvn liquibase:generateChangeLog -Dliquibase.outputChangeLogFile=src/main/resources/liquibase/db-changelog-1.0.xml

