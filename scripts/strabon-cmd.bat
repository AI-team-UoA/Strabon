::=========================================================================================
::
:: Script for running the main classes of Strabon on Windows OS.
:: The main classes of Strabon comprises : QueryOp, , UpdateOp, StoreOp, and  DescribeOp.
::
::=========================================================================================

@echo off

:: command name
set CMD=%~n0%~x0

:: absolute directory name of this command
set LOC=%~p0

set RUNTIME= %LOC%..\runtime

:: runtime package
set PKG=eu.earthobservatory.runtime

:: the underlying database to use (one of `postgis' or `monetdb')
set DATABASE=postgis

:: the main class to run
set CLASS=

:: the hostname at which the database runs
set HOST="localhost"

:: the port at which the database listens
set PORT=

:: the database name to connect to
set DB="strabon"

:: the username for the database connection
set DBUSER=

:: the password for the database connection
set DBPASS=

:: the query to run
set QUERY=

:: the RDF format of the files to store (defaults to ntriples)
set FORMAT=ntriples

:: result format of the query
set RESULT_FORMAT=

:: true to force deletion of locked table, false otherwise
set FORCE_DELETE=false

:: the URI of the named graph into which the RDF files shall be stored
set NAMED_GRAPH=

:: true when inference is enabled
set INFERENCE=

:: predefined queries
set "QUERY_SIZE=SELECT (COUNT(*) as ?C) WHERE {?s ?p ?o}"
set "QUERY_GETALL=SELECT * WHERE {?s ?p ?o}"
set "QUERY_DELETEALL=DELETE {?s ?p ?o} WHERE {?s ?p ?o}"
set "QUERY_HOTSPOT_SIZE=SELECT (COUNT(*) as ?C) WHERE {?h <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#Hotspot>}"
set "QUERY_EXPORT=CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}"

:: further options for java
set JAVA_OPTS=

:: base max memory memory
set /A "BASE_MEMORY=1024"

:: more memory
set MORE_MEMORY=-Xms512M -Xmx1024M

:: much more memory
set MMORE_MEMORY=-Xms512M -Xmx1536M

:: flag for not setting the memory limit more than once
set /A "MEMORY_INCREASED=0"

:: just print what shall be executed
set /A "DEBUG=0"

:: if true, DEBUG has been set in command line,
:: so it must not be overidden
set /A "DEBUG_SAVE=0"

:: Filename containing the prefixes to be included in SPARQL queries
set PREFIXES_FILE=%LOC%prefixes.sparql

:: Prefixes
set "PREFIXES="

:: configuration file for the Strabon connection
set STRABON_CONF=%userprofile%\.strabon

:: file for storage
set FILE=

:: final executable
set STRABON_EXEC=

:: read script options
:while1
set ARG=%1
set IS_FLAG=%ARG:~0,1%
set "FLAG=-"
if "%IS_FLAG%"=="%FLAG%" (

    if "%1"=="-help" (
        call :Help
        exit /b 0
    )

    if "%1"=="-d" (
        if %DEBUG%==0 (
            echo %CMD%: debug is ON
        )
        set /A DEBUG=1
        set /A DEBUG_SAVE=1
    )

    if "%1"=="-m" (
        if %MEMORY_INCREASED% equ 0 (
            set "JAVA_OPTS=%JAVA_OPTS% %MORE_MEMORY%"
            set /A MEMORY_INCREASED=1
        ) else (
            echo %CMD%: memory has already been increased; option -m will be ignored.
        )
    )

    if "%1"=="-M" (
        if %MEMORY_INCREASED% equ 0 (
            set "JAVA_OPTS=%JAVA_OPTS% %MMORE_MEMORY%"
            set /A MEMORY_INCREASED=1
        ) else (
            echo %CMD%: memory has already been increased; option _M will be ignored.
        )
    )

    if "%1"=="-MM" (
        goto :true1
        :true1
        shift
        set /A "MULT=%1"
        set /A RESULT= %MULT% * %BASE_MEMORY%

        if %MEMORY_INCREASED% equ 0 (
            set "JAVA_OPTS=%JAVA_OPTS% -Xms512M -Xmx$%RESULT%M"
            set /A MEMORY_INCREASED=1
        ) else (
            echo %CMD%: memory has already been increased; option -MM will be ignored.
        )
    )

    if "%1"=="-i" (
        set "PREFIXES=PREFIX clc: <http://geo.linkedopendata.gr/corine/ontology#> PREFIX gag: <http://teleios.di.uoa.gr/ontologies/gagKallikratis.rdf#> PREFIX noa: <http://teleios.di.uoa.gr/ontologies/noaOntology.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX strdf: <http://strdf.di.uoa.gr/ontology#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
    )

    if "%1"=="-e" (
        goto :true2
        :true2
        shift
        set DATABASE=%1

        if "%DATABASE%" neq "postgis" (
            if "%DATABASE%" neq "monetdb" (
                echo %CMD%: invalid database engine
			    echo %CMD%: valid database engines are 'postgis' or 'monetdb'
			    exit /b 1
            )
        )
    )

    if "%1"=="-db" (
        goto :true3
        :true3
        shift
        set DB=%1
    )

    if "%1"=="-p" (
        goto :true4
        :true4
        shift
        set PORT=%1
    )

    if "%1"=="-u" (
        goto :true6
        :true6
        shift
        set HOST=%1
    )

    if "%1"=="-h" (
        goto :true7
        :true7
        shift
        set DBUSER=%1
    )

    if "%1"=="-pass" (
        goto :true8
        :true8
        shift
        set DBPASS=%1
    )

    shift
    goto :while1
)

:: set database defaults
if "%DATABASE%"=="postgis" (
    if [%PORT%]==[] (
        set PORT=5432
    )
    if [%DBUSER%]==[] (
        set DBUSER=postgres
    )
    if [%DBPASS%]==[] (
        set DBPASS=postgres
    )
)

if "%DATABASE%"=="monetdb" (
    if [%PORT%]==[] (
        set PORT=50000
    )
    if [%DBUSER%]==[] (
        set DBUSER=monetdb
    )
    if [%DBPASS%]==[] (
        set DBPASS=monetdb
    )
)

:: print configuration/options
if %DEBUG% equ 1 (
	echo %CMD%: printing database connection details
	echo 	 Database Engine : %DATABASE%
	echo 	 Database Name	 : %DB%
	echo 	 Hostname	 : %HOST%
	echo 	 Port		 : %PORT%
	echo 	 Username	 : %DBUSER%
	echo 	 Password	 : %DBPASS%
)


:: determine command to execute
if "%1" neq "help" (
    goto :query
)
if "%1"=="help" (

    goto :true9
    :true9
    shift

    if "%1"=="query" (
        call :help_query
        exit /b 0 
    )

    if "%1"=="update" (
        call :help_update
        exit /b 0 
    )

    if "%1"=="store" (
        call :help_store
        exit /b 0 
    )

    if "%1"=="describe" (
        call :help_describe
        exit /b 0 
    )

    call :Help
    exit /b 0   
)

:query
if "%1" neq "query" (
    goto :update
)
if "%1"=="query" (

    goto :true10
    :true10
    shift
    set "CLASS=QueryOp"

    if "%1"=="--force-delete" (
        goto :truea
        :truea
        shift
        set "FORCE_DELETE=true"        
    )

    if [%1]==[] (
        call :help_query
        exit /b 1
    )

    set QUERY=%1

    if "%QUERY%"=="size" (
        set "QUERY=%QUERY_SIZE%"
    )
    if "%QUERY%"=="hotspots" (
        set "QUERY=%QUERY_HOTSPOT_SIZE%"
    )
    if "%QUERY%"=="all" (
        set "QUERY=%QUERY_GETALL%"
    )

    goto :true11
    :true11
    shift

    if [%1] neq [] (
        set "RESULT_FORMAT=%1"
        set "VALUE="
        goto :true12
        :true12
        call :is_result_format
        
        if %VALUE% equ 0 (
            echo %CMD%: invalid result format "%RESULT_FORMAT%"
			echo %CMD%: valid formats are '???' [default], xml, html, kml, kmz, or geojson
			exit /b 2
        )
    )

    goto :end_class
)

:update
if "%1" neq "update" (
    goto :store
)
if "%1"=="update" (

    goto :true13
    :true13
    shift
    set "CLASS=UpdateOp"

    if [%1]==[] (
        call :help_update
        exit /b 1
    )

    set QUERY=%1

    if "%QUERY%"=="clear" (
        set "QUERY=%QUERY_DELETEALL%"
    )
    goto :end_class
)

:store
if "%1" neq "store" (
    goto :describe
)
if "%1"=="store" (

    goto :true14
    :true14
    shift
    set "CLASS=StoreOp"

    if [%1]==[] (
        call :help_store
        exit /b 1
    )


    :while2
    set "ARG=%1"
    set IS_FLAG=%ARG:~0,1%
    set "FLAG=-"
    if "%IS_FLAG%" neq "%FLAG%" (
        goto :end_while2
    )
    if "%IS_FLAG%" equ "%FLAG%" (

        if "%1"=="-f" (

            goto :true15
            :true15
            shift

            if [%1] neq [] (
                set "FORMAT=%1"
                set "VALUE="
                goto :true16
                :true16
                call :is_format
                
                if %VALUE% equ 1 (
                    goto :start_while2
                )
                if %VALUE% equ 0 (
					echo %CMD%: invalid RDF format "%FORMAT%"
					echo %CMD%: valid RDF formats are 'ntriples', 'n3', 'rdfxml', or 'turtle'
					exit /b 2
                ) 
            ) else (
				echo %CMD%: Option -f requires an RDF format ('ntriples', 'n3', 'rdfxml', or 'turtle')
				exit /b 2
            )
        )

        if "%1"=="-g" (

            goto :true17
            :true17
            shift

            if [%1] neq [] (
                set "NAMED_GRAPH=-g %1"
            ) else (
				echo %CMD%: Option -g requires a URI argument
				exit /b 2
            )

        )

        if "%1"=="--inference" (
            set "INFERENCE=-i true"
        )

        :start_while2
        goto :true18
        :true18
        shift
        goto :while2
    )
    :end_while2
    ::No file was provided
    if [%1]==[] (
        call :help_store
        exit /b 2
    )    

    goto :end_class
)

:describe
if "%1" neq "describe" (
    goto :no_class
)
if "%1"=="describe" (

    goto :true19
    :true19
    shift
    set "CLASS=DescribeOp"

    if [%1]==[] (
        call :help_describe
        exit /b 1
    )

    set QUERY=%1

    if "%QUERY%"=="export" (
        set "QUERY=%QUERY_EXPORT%"
    )

    goto :true20
    :true20
    shift

    if [%1] neq [] (
        set "RESULT_FORMAT=%1"
        set "VALUE="
        goto :true21
        :true21
        call :is_result_format_describe
        
        if %VALUE% equ 0 (
			echo %CMD%: invalid result format "%RESULT_FORMAT%"
			echo %CMD%: valid formats are 'N-Triples', 'RDM/XML', 'N3', 'TURTLE', 'TRIG', 'TRIX', and 'BinaryRDF'
			exit /b 2
        )
    )

    goto :end_class
)

:no_class
if "%1" neq "" (
    goto :incorrect_class
)
if "%1"=="" (
    call :Help
    exit /b 1
)

:incorrect_class
call :Help
echo;
echo %CMD%: unknown command "%1".
exit /b 1


:end_class

:: Prepare the executable command
if "%CLASS%" neq "StoreOp" (
    goto :query_exec
)
if "%CLASS%"=="StoreOp" (
    set FILE1=%1
    set IS_PATH=%ARG:~0,5%
    if "%IS_PATH%"=="file:" (
        set "FILE=%FILE1%"
        goto :ready_file
    )
    if "%IS_PATH%"=="http:" (
        set "FILE=%FILE1%"
        goto :ready_file
    )
    set FILE1=%1
    set IS_PATH=%ARG:~0,1%
    if "%IS_PATH%"=="\" (
        set "FILE=%FILE1%"
        goto :ready_file
    )

    set FILE=%LOC%%FILE1%
    goto :ready_file

    :ready_file
    set STRABON_EXEC=java %JAVA_OPTS% -cp ;..\runtime\target\* %PKG%.%DATABASE%.%CLASS% %HOST% %PORT% %DB% %DBUSER% %DBPASS% "%FILE%" -f %FORMAT% %NAMED_GRAPH% %INFERENCE%
    goto :execute
)

:query_exec
if "%CLASS%" neq "QueryOp" (
    goto :update_or_describe_exec
)
if "%CLASS%"=="QueryOp" (
    set STRABON_EXEC=java %JAVA_OPTS% -cp .;..\runtime\target\* %PKG%.%DATABASE%.%CLASS% %HOST% %PORT% %DB% %DBUSER% %DBPASS% "%PREFIXES% %QUERY%" %FORCE_DELETE% %RESULT_FORMAT%
    goto :execute
)

:update_or_describe_exec
set STRABON_EXEC=java %JAVA_OPTS% -cp ;..\runtime\target\* %PKG%.%DATABASE%.%CLASS% %HOST% %PORT% %DB% %DBUSER% %DBPASS% "%PREFIXES% %QUERY%" %RESULT_FORMAT%
goto :execute    



:execute
echo %STRABON_EXEC%
%STRABON_EXEC%
exit /b 0



:: helper functions

:is_format
if "%FORMAT%"=="ntriples" (
    set /A "VALUE=1"
    exit /b 0
)
if "%FORMAT%"=="n3" (
    set /A "VALUE=1"
    exit /b 0
)
if "%FORMAT%"=="rdfxml" (
    set /A "VALUE=1"
    exit /b 0
)
if "%FORMAT%"=="turtle" (
    set /A "VALUE=1"
    exit /b 0
)
set /A "VALUE=0"
exit /b 0


:is_result_format
if "%RESULT_FORMAT%"=="xml" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="html" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="kml" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="kmz" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="geojson" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="tsv" (
    set /A "VALUE=1"
    exit /b 0
)
set /A "VALUE=0"
exit /b 0


:is_result_format_describe
if "%RESULT_FORMAT%"=="N-Triples" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="RDF/XML" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="N3" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="TURTLE" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="TRIG" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="TRIX" (
    set /A "VALUE=1"
    exit /b 0
)
if "%RESULT_FORMAT%"=="BinaryRDF" (
    set /A "VALUE=1"
    exit /b 0
)
set /A "VALUE=0"
exit /b 0

:: help promts

:Help
echo Usage: %CMD% [OPTIONS] COMMAND ARGS
echo;
echo Interface to execute the main classes of Strabon, such as QueryOp, StoreOp, UpdateOp, DescribeOp, etc.
echo;
echo 	COMMAND	 : one of 'query', 'update', 'store', 'describe', or 'help'
echo 	ARGS	 : arguments according to selected command
echo;
echo OPTIONS can be any of the following (variable names and values are case sensitive)
echo 	-d		: don't run, just print what shall be executed
echo 			  Variable for configuration file: 'DEBUG'
echo 			  Values: 1 or 0
echo 	-m		: use more memory "%MORE_MEMORY%" (useful in "out of memory exceptions")
echo 	-M		: use much more memory "%MMORE_MEMORY%" (useful in "out of memory exceptions")
echo 	-MM MULT	: use MULT * %BASE_MEMORY% MB of memory (useful in "out of memory exceptions")
echo 	-i		: include URI prefixes in the SPARQL query. Prefixes are taken from file
echo 			  'prefixes.sparql'
echo 	-e DATABASE	: the database engine to connect (one of 'postgis' (default) or 'monetdb')
echo 			  Variable for configuration file: 'DATABASE'
echo 			  Values: 'postgis' or 'monetdb'
echo 	-db DB		: the database to connect to (defaults to "%DB%")
echo 			  Variable for configuration file: 'DB'
echo 	-p PORT		: the port to use for the database connection
echo 			: (defaults to 5432 for postgis and 50000 for monetdb)
echo 			  Variable for configuration file: 'PORT'
echo 	-h HOSTNAME	: the hostname to use for the database connection (defaults to "%HOST%")
echo 			  Variable for configuration file: 'HOST'
echo 	-u USERNAME	: the username for the database connection
echo 			  (defaults to 'postgres' for postgis and 'monetdb' for monetdb)"
echo 			  Variable for configuration file: 'DBUSER'
echo 	-pass PASS	: the password for the database connection
echo 			  (defaults to 'postgres' for postgis and 'monetdb' for monetdb)"
echo 			  Variable for configuration file: 'DBPASS'
exit /b 0

:help_query
echo Usage: %CMD% query [OPTIONS] SPARQL_QUERY [RESULT_FORMAT]
echo;
echo Execute a SPARQL query on Strabon.
echo;
echo 	SPARQL_QUERY	: the SPARQL query to execute or an alias name such as the following:
echo 				size: returns the number of triples
echo 				all: returns all triples
echo 				hotspots: returns the number of hotspots
echo 	RESULT_FORMAT	: the format of the result. Possible values are '???' (default), 'xml'
echo 			  'html', 'kml', 'kmz', or 'geojson'
echo;
echo OPTIONS can be one of the following
echo 	--force-delete  : forces deletion of "locked" table (e.g., when Strabon has been
echo 			  ungracefully shutdown)
exit /b 0

:help_update
echo Usage: %CMD% update SPARQL_UPDATE
echo;
echo Execute a SPARQL Update query on Strabon.
echo;
echo 	SPARQL_UPDATE	: the SPARQL update query to execute or an alias name such as the
echo 			  the following:
echo 				clear: deletes all triples
exit /b 0

:help_store
echo Usage: %CMD% store [OPTIONS] FILE...
echo;
echo Store RDF documents in Strabon.
echo;
echo 	FILE	: the file containing the RDF document to store. It can be a filename or a URL,
echo 		  (i.e., file:///tmp/file.nt, http://www.example.org/file.nt,
echo 		  ftp://www.example.org/file.nt, etc.).
echo;
echo OPTIONS can be one of the following
echo 	-f FORMAT : the RDF format of the files to store. The format can be one of the following:
echo 		    'ntriples' (default), 'n3', 'rdfxml', or 'turtle'.
echo 	-g NAMED_GRAPH : the URI of the named graph into which the RDF files shall be stored
echo 	                 (defaults to the default graph).
echo 	--inference : enables inference.
exit /b 0

:help_describe
echo Usage: %CMD% describe DESCRIBE_QUERY [RESULT_FORMAT]
echo;
echo Execute a SPARQL DESCRIBE query on Strabon.
echo;
echo 	DESCRIBE_QUERY	: the SPARQL DESCRIBE query to execute or an alias name such as the following:
echo 				export: returns all triples stored in the database
echo 	RESULT_FORMAT	: the format of the result. Possible values are 'N-Triples', 
echo 			  'RDM/XML', 'N3', 'TURTLE', 'TRIG', 'TRIX', and 'BinaryRDF'
echo 			  (defaults to N-Triples)
exit /b 0