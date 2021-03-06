<%@ page isELIgnored="false"
%><%@ page import="com.fluxtream.domain.*"
%><%@ page import="com.fluxtream.connectors.vos.*"
%><%@ page import="com.fluxtream.connectors.picasa.*"
%><%@ page import="com.fluxtream.connectors.flickr.*"
%><%@ page import="com.fluxtream.utils.Utils"
%><%@ page import="java.util.*"
%><%@ page import="org.joda.time.format.*"
%><%@ page import="com.ocpsoft.pretty.time.PrettyTime"
%><%/* PICASA ------------------------------------------------------------ */%>
<%
if (facet instanceof PicasaPhotoFacetVO) {
	PicasaPhotoFacetVO pf = (PicasaPhotoFacetVO) facet;
%>
	<img style="cursor:pointer" onclick="showPicture('<%=pf.photoUrl %>')" src="<%=pf.thumbnailUrl %>"/>

<%/* FLICKR ------------------------------------------------------------ */%>
<% } else if (facet instanceof FlickrPhotoFacetVO) {
	FlickrPhotoFacetVO pf = (FlickrPhotoFacetVO) facet;%>
	<img style="cursor:pointer" src="<%=pf.photoUrl %>"/>
<% } %>