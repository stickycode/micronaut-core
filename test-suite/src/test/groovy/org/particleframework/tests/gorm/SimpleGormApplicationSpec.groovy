/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.tests.gorm

import grails.gorm.annotation.Entity
import grails.gorm.transactions.Transactional
import org.grails.orm.hibernate.cfg.Settings
import org.particleframework.context.ApplicationContext
import org.particleframework.core.io.socket.SocketUtils
import org.particleframework.runtime.ParticleApplication
import org.particleframework.stereotype.Controller
import org.particleframework.web.router.annotation.Get
import spock.lang.Specification

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class SimpleGormApplicationSpec extends Specification {


    void "test Particle server running"() {
        when:
        System.setProperty(Settings.SETTING_DB_CREATE, "create-drop")
        int newPort = SocketUtils.findAvailableTcpPort()
        ApplicationContext applicationContext = ParticleApplication.run([PersonController] as Class[],'-port',newPort.toString())

        then:
        new URL("http://localhost:$newPort/people").getText(readTimeout:3000) == "People: []"

        cleanup:
        System.setProperty(Settings.SETTING_DB_CREATE, "")
        applicationContext?.stop()
    }

    void "test Particle server running again"() {
        when:
        System.setProperty(Settings.SETTING_DB_CREATE, "create-drop")
        int newPort = SocketUtils.findAvailableTcpPort()
        ApplicationContext applicationContext = ParticleApplication.run([PersonController] as Class[],'-port',newPort.toString())


        then:
        new URL("http://localhost:$newPort/people").getText(readTimeout:3000) == "People: []"

        cleanup:
        System.setProperty(Settings.SETTING_DB_CREATE, "")
        applicationContext?.stop()
    }


}

@Controller('/people')
class PersonController {

    @Transactional
    String index() {
        "People: ${Person.list()}"
    }
}

@Entity
class Person {
    String name
}