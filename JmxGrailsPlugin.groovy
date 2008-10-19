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
Adds JMX supporrt to any Grails application.

'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/JmxGrailsPlugin+Plugin"

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
        // TODO add a bean to spring on the fly

        def configDomain = "GrailsConfig"
        def appDomain = "GrailsApp"

        MBeanExporter exporter = ctx.getBean("exporter")
        exportConfigBeans(exporter, ctx, configDomain)

        exportLogger(ctx, exporter, configDomain)

        exportServices(application, exporter, appDomain)

        exportMBeans(exporter)

    }

    private def exportLogger(ctx, MBeanExporter exporter, configDomain) {

        HierarchyDynamicMBean logMBean = ctx.getBean("log4j")
        exporter.beans."${configDomain}:service=log4j" = ctx.log4j
        logMBean.addLoggerMBean(Logger.getRootLogger().getName())

    }

    private def exportServices(application, MBeanExporter exporter, appDomain) {
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
            exporter.beans."${appDomain}:${objectName}" = service
        }

      }
    }

    private def exportMBeans(MBeanExporter exporter) {
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
}
