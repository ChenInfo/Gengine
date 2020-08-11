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
    TEXT_PLAIN("text/plain"),

    TEXT_MEDIAWIKI("text/mediawiki"),

    TEXT_CSS("text/css"),

    TEXT_CSV("text/csv"),

    TEXT_JAVASCRIPT("text/javascript"),

    XML("text/xml"),

    HTML("text/html"),

    XHTML("application/xhtml+xml"),

    PDF("application/pdf"),

    JSON("application/json"),

    WORD("application/msword"),

    EXCEL("application/vnd.ms-excel"),

    BINARY("application/octet-stream"),

    PPT("application/vnd.ms-powerpoint"),

    APP_DWG("application/dwg"),

    IMG_DWG("image/vnd.dwg"),

    VIDEO_AVI("video/x-msvideo"),

    VIDEO_QUICKTIME("video/quicktime"),

    VIDEO_WMV("video/x-ms-wmv"),

    VIDEO_3GP("video/3gpp"),

    VIDEO_3GP2("video/3gpp2"),

    // Flash
    FLASH("application/x-shockwave-flash"),

    VIDEO_FLV("video/x-flv"),

    APPLICATION_FLA("application/x-fla"),

    VIDEO_MPG("video/mpeg"),

    VIDEO_MP4("video/mp4"),

    IMAGE_GIF("image/gif"),

    IMAGE_JPEG("image/jpeg"),

    IMAGE_RGB("image/x-rgb"),

    IMAGE_SVG("image/svg+xml"),

    IMAGE_PNG("image/png"),

    IMAGE_TIFF("image/tiff"),

    IMAGE_RAW_DNG("image/x-raw-adobe"),

    IMAGE_RAW_3FR("image/x-raw-hasselblad"),

    IMAGE_RAW_RAF("image/x-raw-fuji"),

    IMAGE_RAW_CR2("image/x-raw-canon"),

    IMAGE_RAW_K25("image/x-raw-kodak"),

    IMAGE_RAW_MRW("image/x-raw-minolta"),

    IMAGE_RAW_NEF("image/x-raw-nikon"),

    IMAGE_RAW_ORF("image/x-raw-olympus"),

    IMAGE_RAW_PEF("image/x-raw-pentax"),

    IMAGE_RAW_ARW("image/x-raw-sony"),

    IMAGE_RAW_X3F("image/x-raw-sigma"),

    IMAGE_RAW_RW2("image/x-raw-panasonic"),

    IMAGE_RAW_RWL("image/x-raw-leica"),

    IMAGE_RAW_R3D("image/x-raw-red"),

    APPLICATION_EPS("application/eps"),

    JAVASCRIPT("application/x-javascript"),

    ZIP("application/zip"),

    OPENSEARCH_DESCRIPTION("application/opensearchdescription+xml"),

    ATOM("application/atom+xml"),

    RSS("application/rss+xml"),

    RFC822("message/rfc822"),

    OUTLOOK_MSG("application/vnd.ms-outlook"),

    VISIO("application/vnd.visio"),

    // Adobe
    APPLICATION_ILLUSTRATOR("application/illustrator"),

    APPLICATION_PHOTOSHOP("image/vnd.adobe.photoshop"),

    // Open Document
    OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text"),

    OPENDOCUMENT_TEXT_TEMPLATE("application/vnd.oasis.opendocument.text-template"),

    OPENDOCUMENT_GRAPHICS("application/vnd.oasis.opendocument.graphics"),

    OPENDOCUMENT_GRAPHICS_TEMPLATE("application/vnd.oasis.opendocument.graphics-template"),

    OPENDOCUMENT_PRESENTATION("application/vnd.oasis.opendocument.presentation"),

    OPENDOCUMENT_PRESENTATION_TEMPLATE("application/vnd.oasis.opendocument.presentation-template"),

    OPENDOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet"),

    OPENDOCUMENT_SPREADSHEET_TEMPLATE("application/vnd.oasis.opendocument.spreadsheet-template"),

    OPENDOCUMENT_CHART("application/vnd.oasis.opendocument.chart"),

    OPENDOCUMENT_CHART_TEMPLATE("applicationvnd.oasis.opendocument.chart-template"),

    OPENDOCUMENT_IMAGE("application/vnd.oasis.opendocument.image"),

    OPENDOCUMENT_IMAGE_TEMPLATE("applicationvnd.oasis.opendocument.image-template"),

    OPENDOCUMENT_FORMULA("application/vnd.oasis.opendocument.formula"),

    OPENDOCUMENT_FORMULA_TEMPLATE("applicationvnd.oasis.opendocument.formula-template"),

    OPENDOCUMENT_TEXT_MASTER("application/vnd.oasis.opendocument.text-master"),

    OPENDOCUMENT_TEXT_WEB("application/vnd.oasis.opendocument.text-web"),

    OPENDOCUMENT_DATABASE("application/vnd.oasis.opendocument.database"),

    // Open Office
    OPENOFFICE1_WRITER("application/vnd.sun.xml.writer"),

    OPENOFFICE1_CALC("application/vnd.sun.xml.calc"),

    OPENOFFICE1_DRAW("application/vnd.sun.xml.draw"),

    OPENOFFICE1_IMPRESS("application/vnd.sun.xml.impress"),

    // Open XML
    OPENXML_WORDPROCESSING("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    OPENXML_WORDPROCESSING_MACRO("application/vnd.ms-word.document.macroenabled.12"),
    OPENXML_WORD_TEMPLATE("application/vnd.openxmlformats-officedocument.wordprocessingml.template"),
    OPENXML_WORD_TEMPLATE_MACRO("application/vnd.ms-word.template.macroenabled.12"),
    OPENXML_SPREADSHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    OPENXML_SPREADSHEET_TEMPLATE("application/vnd.openxmlformats-officedocument.spreadsheetml.template"),
    OPENXML_SPREADSHEET_MACRO("application/vnd.ms-excel.sheet.macroenabled.12"),
    OPENXML_SPREADSHEET_TEMPLATE_MACRO("application/vnd.ms-excel.template.macroenabled.12"),
    OPENXML_SPREADSHEET_ADDIN_MACRO("application/vnd.ms-excel.addin.macroenabled.12"),
    OPENXML_SPREADSHEET_BINARY_MACRO("application/vnd.ms-excel.sheet.binary.macroenabled.12"),
    OPENXML_PRESENTATION("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    OPENXML_PRESENTATION_MACRO("application/vnd.ms-powerpoint.presentation.macroenabled.12"),
    OPENXML_PRESENTATION_SLIDESHOW("application/vnd.openxmlformats-officedocument.presentationml.slideshow"),
    OPENXML_PRESENTATION_SLIDESHOW_MACRO("application/vnd.ms-powerpoint.slideshow.macroenabled.12"),
    OPENXML_PRESENTATION_TEMPLATE("application/vnd.openxmlformats-officedocument.presentationml.template"),
    OPENXML_PRESENTATION_TEMPLATE_MACRO("application/vnd.ms-powerpoint.template.macroenabled.12"),
    OPENXML_PRESENTATION_ADDIN("application/vnd.ms-powerpoint.addin.macroenabled.12"),
    OPENXML_PRESENTATION_SLIDE("application/vnd.openxmlformats-officedocument.presentationml.slide"),
    OPENXML_PRESENTATION_SLIDE_MACRO("application/vnd.ms-powerpoint.slide.macroenabled.12"),
    // Star Office
    STAROFFICE5_DRAW("application/vnd.stardivision.draw"),

    STAROFFICE5_CALC("application/vnd.stardivision.calc"),

    STAROFFICE5_IMPRESS("application/vnd.stardivision.impress"),

    STAROFFICE5_IMPRESS_PACKED("application/vnd.stardivision.impress-packed"),

    STAROFFICE5_CHART("application/vnd.stardivision.chart"),

    STAROFFICE5_WRITER("application/vnd.stardivision.writer"),

    STAROFFICE5_WRITER_GLOBAL("application/vnd.stardivision.writer-global"),

    STAROFFICE5_MATH("application/vnd.stardivision.math"),

    // Apple iWorks
    IWORK_KEYNOTE("application/vnd.apple.keynote"),

    IWORK_NUMBERS("application/vnd.apple.numbers"),

    IWORK_PAGES("application/vnd.apple.pages"),

    // WordPerfect
    WORDPERFECT("application/wordperfect"),

    // Audio
    MP3("audio/mpeg"),

    AUDIO_MP4("audio/mp4"),

    VORBIS("audio/vorbis"),

    FLAC("audio/x-flac");

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
