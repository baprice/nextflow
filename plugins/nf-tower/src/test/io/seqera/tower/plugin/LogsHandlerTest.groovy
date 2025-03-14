/*
 * Copyright (c) 2020-2021. Seqera Labs, S.L.
 *
 * All Rights reserved
 *
 */

package io.seqera.tower.plugin

import java.nio.file.Files

import nextflow.Session
import nextflow.exception.AbortOperationException
import spock.lang.Specification
import test.TestHelper

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class LogsHandlerTest extends Specification {

    def 'should init empty files' () {
        when:
        new LogsHandler(Mock(Session), [:])
        then:
        thrown(AbortOperationException)
    }

    def 'should upload cache files' () {
        given:
        def folder = Files.createTempDirectory('test')
        def remote = TestHelper.createInMemTempDir()
        def local = folder.resolve('local'); local.mkdir()
        def outFile = local.resolve('nf-out.txt'); outFile.text = 'out file'
        def logFile = local.resolve('nf-log.txt'); logFile.text = 'log file'
        def tmlFile = local.resolve('nf-tml.txt'); tmlFile.text = 'tml file'
        def cfgFile = local.resolve('tw-config.txt'); cfgFile.text = 'config file'
        def repFile = local.resolve('tw-report.txt'); repFile.text = 'report file'
        and:
        def uuid = UUID.randomUUID().toString()
        and:
        def session = Mock(Session) {getWorkDir() >> remote }
        def ENV = [
                NXF_UUID:uuid,
                NXF_OUT_FILE: outFile.toString(),
                NXF_LOG_FILE: logFile.toString(),
                NXF_TML_FILE: tmlFile.toString(),
                TOWER_CONFIG_FILE: cfgFile.toString(),
                TOWER_REPORTS_FILE: repFile.toString(),
        ]

        when:
        def tower = new LogsHandler(session, ENV)
        then:
        tower.localOutFile == outFile
        tower.localLogFile == logFile
        tower.localTimelineFile == tmlFile
        tower.localTowerConfig == cfgFile
        tower.localTowerReports == repFile
        and:
        tower.remoteWorkDir == remote
        and:
        tower.remoteOutFile == remote.resolve( outFile.name )
        tower.remoteLogFile == remote.resolve( logFile.name )
        tower.remoteTimelineFile == remote.resolve( tmlFile.name )
        tower.remoteTowerConfig == remote.resolve( cfgFile.name )
        tower.remoteTowerReports == remote.resolve( repFile.name )

        when:
        // create local cache fake data
        tower.saveFiles()
        then:
        tower.remoteOutFile.text == outFile.text
        tower.remoteLogFile.text == logFile.text
        tower.remoteTimelineFile.text == tmlFile.text
        tower.remoteTowerConfig.text == cfgFile.text
        tower.remoteTowerReports.text == repFile.text

        cleanup:
        folder?.deleteDir()
    }

}
