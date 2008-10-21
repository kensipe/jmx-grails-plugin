import org.springframework.jmx.support.MBeanServerFactoryBean
import org.springframework.jmx.export.MBeanExporter
import org.hibernate.jmx.StatisticsService

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.apache.log4j.jmx.HierarchyDynamicMBean
import org.apache.log4j.Logger


class JmxGrailsPlugin {
    def version = 0.3
    def dependsOn = [:]

   
    def author = "Ken Sipe"
    def authorEmail = "kensipe@gmail.com"
    def title = "The JMX Grails Plugin"
    def description = '''\
Adds JMX support to any Grails application.
Provides ability to expose any service as an MBean
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/Jmx+Plugin"

    def doWithSpring = {

        // adding the mbean server configuration and export with mbeanserver ref... no exports
        mbeanServer(MBeanServerFactoryBean) {
            locateExistingServerIfPossible = true
        }

        // there is an mbean already defined with hibernate
        hibernateStats(StatisticsService) {
		    statisticsEnabled = true
		    sessionFactory = ref("sessionFactory")
	    }

        log4j(HierarchyDynamicMBean) {
            
        }

        // configure the expo
        exporter(MBeanExporter) {
             server = ref(mbeanServer)
             beans = [:]
        }

    }
   
    def doWithApplicationContext = { ctx ->

        // TODO expose all of jetty

        def configDomain = "GrailsConfig"
        def appDomain = "GrailsApp"

        MBeanExporter exporter = ctx.getBean("exporter")

        // exporting mbeans
        exportConfigBeans(exporter, ctx, configDomain)
        exportLogger(ctx, exporter, configDomain)
        exportServices(application, exporter, appDomain, ctx)

        //
        registerMBeans(exporter)

    }

    private def exportLogger(ctx, MBeanExporter exporter, configDomain) {

        HierarchyDynamicMBean logMBean = ctx.getBean("log4j")
        exporter.beans."${configDomain}:service=log4j" = ctx.log4j
        logMBean.addLoggerMBean(Logger.getRootLogger().getName())

    }

    private def exportServices(application, MBeanExporter exporter, appDomain, ctx) {
      application.serviceClasses?.each {service ->
        def serviceClass = service.getClazz()
        def serviceName = service.shortName
        def objectName = "service=${serviceName}"

        def exposeList = GCU.getStaticPropertyValue(serviceClass, 'expose')

        def gwtExposed = exposeList?.find { it.startsWith('jmx') }
        if (gwtExposed) {
            // change service name if provided by jmx:objectname
            def m = gwtExposed =~ 'jmx:(.*)'
            if(m) {
                objectName = "${m[0][1]}"
            // TODO need to check for malformed objectnames and fall back to standard service name
            }
            //perhaps these should be limited to singletons
            exporter.beans."${appDomain}:${objectName}" =  ctx.getBean(service.getPropertyName())
        }

          // TODO 1. Remove the groovy exposure to jmx... just publicly declared methods of service or service interface
                    // need to think about this one... but perhaps rewritting the reflection calls would do it ???
                    // or writing a class on the fly 
          // TODO 2. Add ability to add meta data... perhaps through an MBeanInfo Assembler

      }
    }

    private def registerMBeans(MBeanExporter exporter) {
        exporter.unregisterBeans()
        exporter.registerBeans()
    }

    private def exportConfigBeans(MBeanExporter exporter, ctx, configDomain) {
        exporter.beans."${configDomain}:service=statistics,type=hibernate" = ctx.hibernateStats
        exporter.beans."${configDomain}:service=datasource" = ctx.dataSource

    }

    def doWithWebDescriptor = { xml ->

    }
	                                      
    def doWithDynamicMethods = { ctx ->

    }
	
    def onChange = { event ->

    }

    def onConfigChange = { event ->

    }

// TODO I would definitely like to constraint the jmx exposed interfaces with something like
// look at gwt...
// 

// Iterate through the methods declared by the Grails service,
// adding the appropriate ones to the interface definitions.
/*serviceWrapper.clazz.declaredMethods.each { Method method ->
    // Skip non-public, static, Groovy, and property methods.
    if (!Modifier.isPublic(method.modifiers) ||
            Modifier.isStatic(method.modifiers) ||
            GROOVY_METHODS.contains(method.name) ||
            propMethods.contains(method)) {
        return
    }*/
}
