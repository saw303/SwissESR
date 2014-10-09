package io.wangler.esr

/**
 * @author Silvio Wangler
 * @since 0.3
 */
class SvgParser {

    public static void main(String[] args) {

        def resource = SvgParser.class.getResource('/esr.svg')

        def root = new XmlParser().parseText(resource.text)

        def allRecords = root.depthFirst().findAll { it.name().qualifiedName == 'tspan' && it.text()}

        for (record in allRecords) {
            println "${record.@id}: ${record.text()}"
            //record.value = 'yolo'
        }

        //new XmlNodePrinter(new PrintWriter(System.out)).print(root)
    }
}