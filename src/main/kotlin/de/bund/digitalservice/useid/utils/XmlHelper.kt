package de.bund.digitalservice.useid.utils

import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

object XmlHelper {
    /**
     * This method marshals any element that is annotated with [javax.xml.bind.annotation.XmlRootElement].
     *
     * @param object the annotated XML object
     * @return the string representation of the XML object
     */
    fun marshalObject(`object`: Any): String {
        return try {
            val jc = JAXBContext.newInstance(`object`.javaClass)
            val marshaller = jc.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
            val stringWriter = StringWriter()
            marshaller.marshal(`object`, stringWriter)
            stringWriter.toString()
        } catch (e: JAXBException) {
            throw IllegalStateException("error while marshalling class " + `object`.javaClass.name, e)
        }
    }
}
