def code = '0100000002852>000000021009384900197001017+ 010438848>'
def x = 778.41864 as BigDecimal

def sb = new StringBuilder()

code.reverse().eachWithIndex { c, index -> 

    def text = """
    <text
       xml:space="preserve"
       style="font-size:12px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;line-height:125%;letter-spacing:0px;word-spacing:0px;fill:#000000;fill-opacity:1;stroke:none;font-family:OCRB;-inkscape-font-specification:OCRB"
       x="${x}"
       y="945.08527"
       id="text3002-0"
       sodipodi:linespacing="125%"
       transform="scale(0.95167663,1.0507771)"><tspan
         sodipodi:role="line"
         id="code-${53-index}"
         x="${x}"
         y="945.08527">${groovy.xml.XmlUtil.escapeXml(c)}</tspan></text>   
    """

    sb << text.toString()
    sb << '\n'
    x = x - (10 as BigDecimal)
}

def file = new File(System.properties['java.io.tmpdir'], 'yolo.xml')

if (file.exists()) file.delete()

file << sb.toString()

println "Save file to ${file.absolutePath}"