package org.gengine.content.mediatype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;

/**
 * Convenience enum for to more easily use media types with {@link FileMediaTypeServiceImpl}.
 */
public enum FileMediaType
{
    MEDIATYPE_TEXT_PLAIN("text/plain"),

    MEDIATYPE_TEXT_MEDIAWIKI("text/mediawiki"),

    MEDIATYPE_TEXT_CSS("text/css"),

    MEDIATYPE_TEXT_CSV("text/csv"),

    MEDIATYPE_TEXT_JAVASCRIPT("text/javascript"),

    MEDIATYPE_XML("text/xml"),

    MEDIATYPE_HTML("text/html"),

    MEDIATYPE_XHTML("application/xhtml+xml"),

    MEDIATYPE_PDF("application/pdf"),

    MEDIATYPE_JSON("application/json"),

    MEDIATYPE_WORD("application/msword"),

    MEDIATYPE_EXCEL("application/vnd.ms-excel"),

    MEDIATYPE_BINARY("application/octet-stream"),

    MEDIATYPE_PPT("application/vnd.ms-powerpoint"),

    MEDIATYPE_APP_DWG("application/dwg"),

    MEDIATYPE_IMG_DWG("image/vnd.dwg"),

    MEDIATYPE_VIDEO_AVI("video/x-msvideo"),

    MEDIATYPE_VIDEO_QUICKTIME("video/quicktime"),

    MEDIATYPE_VIDEO_WMV("video/x-ms-wmv"),

    MEDIATYPE_VIDEO_3GP("video/3gpp"),

    MEDIATYPE_VIDEO_3GP2("video/3gpp2"),

    // Flash
    MEDIATYPE_FLASH("application/x-shockwave-flash"),

    MEDIATYPE_VIDEO_FLV("video/x-flv"),

    MEDIATYPE_APPLICATION_FLA("application/x-fla"),

    MEDIATYPE_VIDEO_MPG("video/mpeg"),

    MEDIATYPE_VIDEO_MP4("video/mp4"),

    MEDIATYPE_IMAGE_GIF("image/gif"),

    MEDIATYPE_IMAGE_JPEG("image/jpeg"),

    MEDIATYPE_IMAGE_RGB("image/x-rgb"),

    MEDIATYPE_IMAGE_SVG("image/svg+xml"),

    MEDIATYPE_IMAGE_PNG("image/png"),

    MEDIATYPE_IMAGE_TIFF("image/tiff"),

    MEDIATYPE_IMAGE_RAW_DNG("image/x-raw-adobe"),

    MEDIATYPE_IMAGE_RAW_3FR("image/x-raw-hasselblad"),

    MEDIATYPE_IMAGE_RAW_RAF("image/x-raw-fuji"),

    MEDIATYPE_IMAGE_RAW_CR2("image/x-raw-canon"),

    MEDIATYPE_IMAGE_RAW_K25("image/x-raw-kodak"),

    MEDIATYPE_IMAGE_RAW_MRW("image/x-raw-minolta"),

    MEDIATYPE_IMAGE_RAW_NEF("image/x-raw-nikon"),

    MEDIATYPE_IMAGE_RAW_ORF("image/x-raw-olympus"),

    MEDIATYPE_IMAGE_RAW_PEF("image/x-raw-pentax"),

    MEDIATYPE_IMAGE_RAW_ARW("image/x-raw-sony"),

    MEDIATYPE_IMAGE_RAW_X3F("image/x-raw-sigma"),

    MEDIATYPE_IMAGE_RAW_RW2("image/x-raw-panasonic"),

    MEDIATYPE_IMAGE_RAW_RWL("image/x-raw-leica"),

    MEDIATYPE_IMAGE_RAW_R3D("image/x-raw-red"),

    MEDIATYPE_APPLICATION_EPS("application/eps"),

    MEDIATYPE_JAVASCRIPT("application/x-javascript"),

    MEDIATYPE_ZIP("application/zip"),

    MEDIATYPE_OPENSEARCH_DESCRIPTION("application/opensearchdescription+xml"),

    MEDIATYPE_ATOM("application/atom+xml"),

    MEDIATYPE_RSS("application/rss+xml"),

    MEDIATYPE_RFC822("message/rfc822"),

    MEDIATYPE_OUTLOOK_MSG("application/vnd.ms-outlook"),

    MEDIATYPE_VISIO("application/vnd.visio"),

    // Adobe
    MEDIATYPE_APPLICATION_ILLUSTRATOR("application/illustrator"),

    MEDIATYPE_APPLICATION_PHOTOSHOP("image/vnd.adobe.photoshop"),

    // Open Document
    MEDIATYPE_OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text"),

    MEDIATYPE_OPENDOCUMENT_TEXT_TEMPLATE("application/vnd.oasis.opendocument.text-template"),

    MEDIATYPE_OPENDOCUMENT_GRAPHICS("application/vnd.oasis.opendocument.graphics"),

    MEDIATYPE_OPENDOCUMENT_GRAPHICS_TEMPLATE("application/vnd.oasis.opendocument.graphics-template"),

    MEDIATYPE_OPENDOCUMENT_PRESENTATION("application/vnd.oasis.opendocument.presentation"),

    MEDIATYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE("application/vnd.oasis.opendocument.presentation-template"),

    MEDIATYPE_OPENDOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet"),

    MEDIATYPE_OPENDOCUMENT_SPREADSHEET_TEMPLATE("application/vnd.oasis.opendocument.spreadsheet-template"),

    MEDIATYPE_OPENDOCUMENT_CHART("application/vnd.oasis.opendocument.chart"),

    MEDIATYPE_OPENDOCUMENT_CHART_TEMPLATE("applicationvnd.oasis.opendocument.chart-template"),

    MEDIATYPE_OPENDOCUMENT_IMAGE("application/vnd.oasis.opendocument.image"),

    MEDIATYPE_OPENDOCUMENT_IMAGE_TEMPLATE("applicationvnd.oasis.opendocument.image-template"),

    MEDIATYPE_OPENDOCUMENT_FORMULA("application/vnd.oasis.opendocument.formula"),

    MEDIATYPE_OPENDOCUMENT_FORMULA_TEMPLATE("applicationvnd.oasis.opendocument.formula-template"),

    MEDIATYPE_OPENDOCUMENT_TEXT_MASTER("application/vnd.oasis.opendocument.text-master"),

    MEDIATYPE_OPENDOCUMENT_TEXT_WEB("application/vnd.oasis.opendocument.text-web"),

    MEDIATYPE_OPENDOCUMENT_DATABASE("application/vnd.oasis.opendocument.database"),

    // Open Office
    MEDIATYPE_OPENOFFICE1_WRITER("application/vnd.sun.xml.writer"),

    MEDIATYPE_OPENOFFICE1_CALC("application/vnd.sun.xml.calc"),

    MEDIATYPE_OPENOFFICE1_DRAW("application/vnd.sun.xml.draw"),

    MEDIATYPE_OPENOFFICE1_IMPRESS("application/vnd.sun.xml.impress"),

    // Open XML
    MEDIATYPE_OPENXML_WORDPROCESSING("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    MEDIATYPE_OPENXML_WORDPROCESSING_MACRO("application/vnd.ms-word.document.macroenabled.12"),
    MEDIATYPE_OPENXML_WORD_TEMPLATE("application/vnd.openxmlformats-officedocument.wordprocessingml.template"),
    MEDIATYPE_OPENXML_WORD_TEMPLATE_MACRO("application/vnd.ms-word.template.macroenabled.12"),
    MEDIATYPE_OPENXML_SPREADSHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    MEDIATYPE_OPENXML_SPREADSHEET_TEMPLATE("application/vnd.openxmlformats-officedocument.spreadsheetml.template"),
    MEDIATYPE_OPENXML_SPREADSHEET_MACRO("application/vnd.ms-excel.sheet.macroenabled.12"),
    MEDIATYPE_OPENXML_SPREADSHEET_TEMPLATE_MACRO("application/vnd.ms-excel.template.macroenabled.12"),
    MEDIATYPE_OPENXML_SPREADSHEET_ADDIN_MACRO("application/vnd.ms-excel.addin.macroenabled.12"),
    MEDIATYPE_OPENXML_SPREADSHEET_BINARY_MACRO("application/vnd.ms-excel.sheet.binary.macroenabled.12"),
    MEDIATYPE_OPENXML_PRESENTATION("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    MEDIATYPE_OPENXML_PRESENTATION_MACRO("application/vnd.ms-powerpoint.presentation.macroenabled.12"),
    MEDIATYPE_OPENXML_PRESENTATION_SLIDESHOW("application/vnd.openxmlformats-officedocument.presentationml.slideshow"),
    MEDIATYPE_OPENXML_PRESENTATION_SLIDESHOW_MACRO("application/vnd.ms-powerpoint.slideshow.macroenabled.12"),
    MEDIATYPE_OPENXML_PRESENTATION_TEMPLATE("application/vnd.openxmlformats-officedocument.presentationml.template"),
    MEDIATYPE_OPENXML_PRESENTATION_TEMPLATE_MACRO("application/vnd.ms-powerpoint.template.macroenabled.12"),
    MEDIATYPE_OPENXML_PRESENTATION_ADDIN("application/vnd.ms-powerpoint.addin.macroenabled.12"),
    MEDIATYPE_OPENXML_PRESENTATION_SLIDE("application/vnd.openxmlformats-officedocument.presentationml.slide"),
    MEDIATYPE_OPENXML_PRESENTATION_SLIDE_MACRO("application/vnd.ms-powerpoint.slide.macroenabled.12"),
    // Star Office
    MEDIATYPE_STAROFFICE5_DRAW("application/vnd.stardivision.draw"),

    MEDIATYPE_STAROFFICE5_CALC("application/vnd.stardivision.calc"),

    MEDIATYPE_STAROFFICE5_IMPRESS("application/vnd.stardivision.impress"),

    MEDIATYPE_STAROFFICE5_IMPRESS_PACKED("application/vnd.stardivision.impress-packed"),

    MEDIATYPE_STAROFFICE5_CHART("application/vnd.stardivision.chart"),

    MEDIATYPE_STAROFFICE5_WRITER("application/vnd.stardivision.writer"),

    MEDIATYPE_STAROFFICE5_WRITER_GLOBAL("application/vnd.stardivision.writer-global"),

    MEDIATYPE_STAROFFICE5_MATH("application/vnd.stardivision.math"),

    // Apple iWorks
    MEDIATYPE_IWORK_KEYNOTE("application/vnd.apple.keynote"),

    MEDIATYPE_IWORK_NUMBERS("application/vnd.apple.numbers"),

    MEDIATYPE_IWORK_PAGES("application/vnd.apple.pages"),

    // WordPerfect
    MEDIATYPE_WORDPERFECT("application/wordperfect"),

    // Audio
    MEDIATYPE_MP3("audio/mpeg"),

    MEDIATYPE_AUDIO_MP4("audio/mp4"),

    MEDIATYPE_VORBIS("audio/vorbis"),

    MEDIATYPE_FLAC("audio/x-flac");

    private static final Log logger = LogFactory.getLog(FileMediaType.class);

    public static final String PREFIX_APPLICATION = "application/";
    public static final String PREFIX_AUDIO = "audio/";
    public static final String PREFIX_IMAGE = "image/";
    public static final String PREFIX_MESSAGE = "message/";
    public static final String PREFIX_MODEL = "model/";
    public static final String PREFIX_MULTIPART = "multipart/";
    public static final String PREFIX_TEXT = "text/";
    public static final String PREFIX_VIDEO = "video/";

    public static final String EXTENSION_BINARY = "bin";


    private MimeType tikaMimeType;
    private String mediaType;

    private FileMediaType(String mediaType)
    {
        this.mediaType = mediaType;
    }

    /**
     * Gets the underlying Tika MimeType based on the FileMediaType's mediaType string
     *
     * @return the Tika MimeType
     */
    public MimeType getTikaMimeType()
    {
        if (tikaMimeType == null)
        {
            try
            {
                tikaMimeType = FileMediaType.SERVICE.getTikaMimeType(mediaType);
            }
            catch (MimeTypeException e)
            {
                logger.debug("Could not get Tika MimeType for " + mediaType);
            }
        }
        return tikaMimeType;
    }

    /**
     * Gets the media type string, i.e. "text/plain" from the Tika MimeType if available,
     * otherwise uses the raw mediaType string.
     *
     * @return the media type string
     */
    public String getMediaType()
    {
        if (getTikaMimeType() == null)
        {
            return mediaType;
        }
        return getTikaMimeType().toString();
    }

    /**
     * Gets the default extension via the {@link FileMediaTypeServiceImpl}
     *
     * @return the file extension, i.e. ".txt"
     */
    public String getExtension()
    {
        return FileMediaType.SERVICE.getExtension(getMediaType());
    }

    public static FileMediaTypeServiceImpl SERVICE = new FileMediaTypeServiceImpl(null);

}
