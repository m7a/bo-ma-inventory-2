<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
		xmlns:bibo="http://purl.org/ontology/bibo/"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:dcterms="http://purl.org/dc/terms/"
		xmlns:isbd="http://iflastandards.info/ns/isbd/elements/"
		xmlns:rdau="http://rdaregistry.info/Elements/u/"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text" indent="no" encoding="UTF-8"/>

	<xsl:strip-space elements="*" />

	<!-- see http://stackoverflow.com/questions/324822 -->
	<!-- see http://www.dpawson.co.uk/xsl/sect2/normalise.html -->
	<xsl:template match="node/@TEXT|text()">
		<!-- alt. "concat('&#x20;',normalize-space(.),'&#x20;')" -->
		<xsl:if test="normalize-space(.)">
			<xsl:value-of select="translate(translate(.,
						'&#x0a;', ' '), '&#9;', ' ')"/>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="rdf:RDF">
<xsl:text>
BEGIN TRANSACTION;
CREATE TABLE books (
	isbn13       BIGINT,
	isbn10       BIGINT,
	author       VARCHAR(64),
	title        VARCHAR(256),
	year         VARCHAR(32),
	publisher    VARCHAR(128),
	location     VARCHAR(64),
	pages        INTEGER
);
INSERT INTO books (isbn13, isbn10, author, title, year, publisher, location, pages) VALUES </xsl:text>
		<xsl:for-each select="rdf:Description">
			<xsl:text>(</xsl:text>
			<xsl:call-template name="nrtpl"><xsl:with-param name="a" select="bibo:isbn13[1]"/></xsl:call-template>
			<xsl:call-template name="nrtpl"><xsl:with-param name="a" select="bibo:isbn10[1]"/></xsl:call-template>
			<xsl:text>NULL,</xsl:text>
			<xsl:call-template name="strtpl"><xsl:with-param name="a" select="dc:title[1]"/></xsl:call-template>
			<xsl:call-template name="strtpl"><xsl:with-param name="a" select="dcterms:issued[1]"/></xsl:call-template>
			<xsl:call-template name="strtpl"><xsl:with-param name="a" select="dc:publisher[1]"/></xsl:call-template>
			<!-- location -->
			<xsl:call-template name="strtpl"><xsl:with-param name="a" select="rdau:P60163[1]"/></xsl:call-template>

			<!-- pages TODO MAKE SURE IT IS A NUMBER (Mikorfiches ist keien Zahl) -->
			<xsl:variable name="cnt" select="translate(string(isbd:P1053[1]/text()), '[]ABCDEFGHIJKLMNOPQRSTUVWXYZ.,abcdefghijklmnopqrstuvwxyz', '')"/>
			<xsl:choose>
				<xsl:when test="number($cnt)"><xsl:value-of select="$cnt"/></xsl:when>
				<xsl:otherwise>NULL</xsl:otherwise>
			</xsl:choose>

			<xsl:text>),&#x0a;</xsl:text>
		</xsl:for-each>
		<xsl:text>COMMIT;</xsl:text>
	</xsl:template>

	<xsl:template name="nrtpl"> <!--match="bibo:isbn13 | bibo:isbn10">-->
		<xsl:param name="a"/>
		<xsl:choose>
			<xsl:when test="$a"><xsl:apply-templates select="$a"/></xsl:when>
			<xsl:otherwise><xsl:text>NULL</xsl:text></xsl:otherwise>
		</xsl:choose>
		<xsl:text>,</xsl:text>
	</xsl:template>

	<xsl:template name="strtpl">
		<xsl:param name="a"/>
		<xsl:choose>
			<xsl:when test="$a">
				<xsl:text>'</xsl:text>
				<xsl:call-template name="dai_str_replace">
					<xsl:with-param name="text">
						<xsl:apply-templates select="$a"/>
					</xsl:with-param>
					<xsl:with-param name="replace" select="&quot;&apos;&quot;"/>
					<xsl:with-param name="by" select="&quot;&apos;&apos;&quot;"/>
				</xsl:call-template>
				<xsl:text>'</xsl:text>
			</xsl:when>
			<xsl:otherwise>NULL</xsl:otherwise>
		</xsl:choose>
		<xsl:text>,</xsl:text>
	</xsl:template>

	<!--
		String replacing function from
		http://geekswithblogs.net/Erik/archive/2008/04/01/120915.aspx
	-->
	<xsl:template name="dai_str_replace">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="by"/>
		<xsl:choose>
			<xsl:when test="contains($text, $replace)">
				<xsl:value-of select="substring-before($text,
								$replace)"/>
				<xsl:value-of select="$by"/>
				<xsl:call-template name="dai_str_replace">
					<xsl:with-param name="text"
						select="substring-after($text,
								$replace)"/>
					<xsl:with-param name="replace"
							select="$replace"/>
					<xsl:with-param name="by" select="$by"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
