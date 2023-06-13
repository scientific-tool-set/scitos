<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ais="http://www.hermeneutix.org/schema/ais/1.0">
    <xsl:output method="html" indent="no" />
    <xsl:template match="ais:AisProject">
        <html>
            <head>
                <title>Autobiographical Interview Scoring (presented by SciToS)</title>
                <style media="all" type="text/css">
                    <xsl:text>body { background: #FFF; }
.accordion {
    overflow: hidden;
    padding: 0.5em;
}
.accordion section {
    overflow: hidden;
    margin: 0.2em 0;
    width: 100%;
}
.accordion section h2 {
    position: relative;
    left: 0.2em;
    top: -0.66em;
}
.accordion section h2 a {
    display: block;
    font-weight: normal;
    text-decoration: none;
}
.accordion section .section-content { padding: 1em; }
table { width: 100%; }
tr:nth-child(2n) td { background: #E0E0E0; }
th { padding: 0.25em 0.1em; }
td { padding: 0.25em 0.5em; }
td.number { text-align: right; }
.token {
    display: inline-block;
    margin: 0.5em 0;
}
.token span {
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
}
span.detail-start { float: left; }
span.detail-category {
    text-align: left;
    margin: 0 0.5em;
}
span.detail-end { float: right; }
.token label {
    display: block;
    clear: both;
    bottom: 0;
    text-align: center;
    padding-bottom: 0.15em;
}
.token.detail-assigned label {
    border-top: 0.15em black solid;
    padding-top: 0.25em;
}
.token:not(.detail-assigned) label { padding-top: 0.4em; }
.token label.detail-start {
    border-left: 0.15em black solid;
    padding-left: 0.5em;
}
.token label:not(.detail-start) { padding-left: 0.65em; }
.token label.detail-end {
    border-right: 0.15em black solid;
    padding-right: 0.5em;
}
.token label:not(.detail-end) { padding-right: 0.65em; }
</xsl:text>
                </style>
                <style media="screen" type="text/css">
                    <xsl:text>.accordion {
    width: 95%;
    margin: 0 auto;
    background: #888;
}
.accordion section {
    height: 2.5em;
    background: #666;
}
.accordion section:target {
    background: #FFF;
    min-height: 6em;
    height: auto;
}
.accordion section:not(:target):hover {
    background: #444;
    cursor: pointer;
}
.accordion section:target h2 {
    width: 100%;
    padding-top: 1em;
    padding-left: 0.8em;
}
.accordion section h2 a {
    padding: 0.5em 0.6em 0 0.6em;
    font-size: 16px;
    color: #EEE;
}
.accordion section:target h2 a {
    padding: 0;
    font-size: 20px;
    font-weight: bold;
    color: #333;
    cursor: initial;
}
.accordion section:not(:target) .section-content { display:none; }
.accordion section:target .section-content { padding-top: 0; }
.paragraph { padding: 0.25em 0; }</xsl:text>
                    <xsl:apply-templates select="//ais:Category" />
                </style>
                <style media="print" type="text/css">
                    <xsl:text>.accordion { width: 100%; }
.accordion section { width: 100%; }
.accordion section:not(:last-child) { border-bottom: #000 1px solid; }
.accordion section h2 {
    width: 100%;
    padding-top: 1em;
    padding-left: 1.5em;
    margin-bottom: 0;
}
.accordion section h2 a {
    padding: 0;
    font-size: 20px;
    font-weight: bold;
    color: #000;
}
.paragraph { padding: 0; }</xsl:text>
                </style>
            </head>
            <body>
                <xsl:apply-templates select="ais:Interviews" />
            </body>
        </html>
    </xsl:template>
    <xsl:template match="ais:Category">
        <xsl:text>
.token.detail-</xsl:text>
        <xsl:value-of select="position()" />
        <xsl:value-of select="' label { border-top-color: #'" />
        <xsl:choose>
            <xsl:when test="@color">
                <xsl:variable name="rgb-values" select="translate(@color,'rgb() ','')" />
                <xsl:call-template name="decimal-to-hex">
                    <xsl:with-param name="decimal" select="substring-before($rgb-values,',')" />
                </xsl:call-template>
                <xsl:variable name="gb-values" select="substring-after($rgb-values,',')" />
                <xsl:call-template name="decimal-to-hex">
                    <xsl:with-param name="decimal" select="substring-before($gb-values,',')" />
                </xsl:call-template>
                <xsl:call-template name="decimal-to-hex">
                    <xsl:with-param name="decimal" select="substring-after($gb-values,',')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'000'" />
            </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="'; }'" />
    </xsl:template>
    <xsl:template name="decimal-to-hex">
        <xsl:param name="decimal" />
        <xsl:variable name="hex-literals" select="'0123456789ABCDEF'" />
        <xsl:value-of select="substring($hex-literals,1+floor($decimal div 16),1)" />
        <xsl:value-of select="substring($hex-literals,1+($decimal mod 16),1)" />
    </xsl:template>
    <xsl:template match="ais:Interviews">
        <div class="accordion">
            <section id="results">
                <h2>
                    <a href="#results">Results</a>
                </h2>
                <div class="section-content">
                    <table>
                        <tr>
                            <th>Interview</th>
                            <th title="Number of Tokens with assigned Detail Category">Token Count</th>
                            <xsl:for-each select="//ais:Categories/ais:Category">
                                <xsl:call-template name="insert-result-headers">
                                    <xsl:with-param name="category" select="." />
                                </xsl:call-template>
                            </xsl:for-each>
                        </tr>
                        <xsl:for-each select="ais:Interview">
                            <tr>
                                <td>
                                    <xsl:value-of select="@participant" />
                                    <xsl:if test="1 &lt; count(../ais:Interview[@participant=current()/@participant])">
                                        <xsl:value-of select="' ('" />
                                        <xsl:value-of select="@index" />
                                        <xsl:value-of select="')'" />
                                    </xsl:if>
                                </td>
                                <td class="number">
                                    <xsl:value-of select="count(.//ais:Detail[@code]/ais:Token)" />
                                </td>
                                <xsl:variable name="interview" select="." />
                                <xsl:for-each select="//ais:Categories/ais:Category">
                                    <xsl:call-template name="insert-result-values">
                                        <xsl:with-param name="interview" select="$interview" />
                                        <xsl:with-param name="category" select="." />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </tr>
                        </xsl:for-each>
                    </table>
                </div>
            </section>
            <xsl:apply-templates select="ais:Interview" />
        </div>
    </xsl:template>
    <xsl:template name="insert-result-headers">
        <xsl:param name="category" />
        <th>
            <xsl:if test="$category/@name">
                <xsl:attribute name="title">
                    <xsl:value-of select="$category/@name" />
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$category/@code" />
        </th>
        <xsl:for-each select="$category/ais:Category">
            <xsl:call-template name="insert-result-headers">
                <xsl:with-param name="category" select="." />
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="insert-result-values">
        <xsl:param name="interview" />
        <xsl:param name="category" />
        <td class="number">
            <xsl:choose>
                <xsl:when test="$category/ais:Category">
                    <xsl:variable name="childCodes">
                        <xsl:for-each select="$category/ais:Category[not(Category)]">
                            <xsl:value-of select="concat(' ',@code,' ')" />
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:value-of select="count($interview//ais:Detail[@code and contains($childCodes,concat(' ',@code,' '))])" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="count($interview//ais:Detail[@code=$category/@code])" />
                </xsl:otherwise>
            </xsl:choose>
        </td>
        <xsl:for-each select="$category/ais:Category">
            <xsl:call-template name="insert-result-values">
                <xsl:with-param name="interview" select="$interview" />
                <xsl:with-param name="category" select="." />
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="ais:Interview">
        <xsl:variable name="id">
            <xsl:value-of select="'interview-'" />
            <xsl:value-of select="position()" />
        </xsl:variable>
        <section>
            <xsl:attribute name="id">
                <xsl:value-of select="$id" />
            </xsl:attribute>
            <h2>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="'#'" />
                        <xsl:value-of select="$id" />
                    </xsl:attribute>
                    <xsl:value-of select="@participant" />
                    <xsl:if test="1 &lt; count(../ais:Interview[@participant=current()/@participant])">
                        <xsl:value-of select="' ('" />
                        <xsl:value-of select="@index" />
                        <xsl:value-of select="')'" />
                    </xsl:if>
                </a>
            </h2>
            <div class="section-content">
                <xsl:apply-templates select="ais:Paragraph" />
            </div>
        </section>
    </xsl:template>
    <xsl:template match="ais:Paragraph">
        <div class="paragraph">
            <xsl:apply-templates select=".//ais:Token" />
        </div>
    </xsl:template>
    <xsl:template match="ais:Token">
        <div>
            <xsl:attribute name="class">
                <xsl:if test="local-name(..) = 'Detail' and ../@code">
                    <xsl:value-of select="'detail-assigned detail-'" />
                    <xsl:value-of select="1 + count(//ais:Category[@code = current()/../@code]/preceding::ais:Category)" />
                    <xsl:value-of select="' '" />
                </xsl:if>
                <xsl:value-of select="'token'" />
            </xsl:attribute>
            <xsl:variable name="showDetailStart"
                select="(not(../@code) and local-name(preceding-sibling::*[1]) = 'Detail') or (count(preceding-sibling::ais:Token) = 0 and position() &gt; 1)" />
            <xsl:variable name="showDetailEnd"
                select="(not(../@code) and local-name(following-sibling::*[1]) = 'Detail') or (count(following-sibling::ais:Token) = 0 and position() &lt; last())" />
            <xsl:if test="$showDetailStart and local-name(../..) = 'Detail'">
                <span class="detail-start">
                    <xsl:value-of select="'('" />
                </span>
            </xsl:if>
            <span class="detail-category">
                <xsl:if test="local-name(..) = 'Detail' and ../@code and count(preceding-sibling::ais:Token) = 0">
                    <xsl:value-of select="../@code" />
                </xsl:if>
            </span>
            <xsl:if test="$showDetailEnd and local-name(../..) = 'Detail'">
                <span class="detail-end">
                    <xsl:value-of select="')'" />
                </span>
            </xsl:if>
            <label>
                <xsl:if test="$showDetailStart or $showDetailEnd">
                    <xsl:attribute name="class">
                        <xsl:if test="$showDetailStart">
                            <xsl:value-of select="'detail-start '" />
                        </xsl:if>
                        <xsl:if test="$showDetailEnd">
                            <xsl:value-of select="'detail-end'" />
                        </xsl:if>
                    </xsl:attribute>
                </xsl:if>
                <xsl:value-of select="./text()" />
            </label>
        </div>
    </xsl:template>
</xsl:stylesheet>