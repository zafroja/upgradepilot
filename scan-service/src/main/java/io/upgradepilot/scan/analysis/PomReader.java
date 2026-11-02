package io.upgradepilot.scan.analysis;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Minimal, safe pom.xml reader (no Maven dependency, XXE disabled). */
public final class PomReader {

    public record PomInfo(String parentArtifactId, String parentVersion, Map<String, String> properties) {

        public String javaVersion() {
            for (String key : new String[]{"java.version", "maven.compiler.release",
                    "maven.compiler.source", "maven.compiler.target"}) {
                String v = properties.get(key);
                if (v != null && !v.isBlank()) return v.trim();
            }
            return null;
        }

        public String springBootVersion() {
            if ("spring-boot-starter-parent".equals(parentArtifactId)) return parentVersion;
            return properties.get("spring-boot.version");
        }
    }

    private PomReader() {
    }

    public static Optional<PomInfo> read(Path pom) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            Element project = f.newDocumentBuilder().parse(pom.toFile()).getDocumentElement();

            String parentArtifact = null;
            String parentVersion = null;
            Element parent = child(project, "parent");
            if (parent != null) {
                parentArtifact = text(child(parent, "artifactId"));
                parentVersion = text(child(parent, "version"));
            }

            Map<String, String> props = new HashMap<>();
            Element properties = child(project, "properties");
            if (properties != null) {
                NodeList nodes = properties.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i) instanceof Element e) {
                        props.put(e.getNodeName(), e.getTextContent().trim());
                    }
                }
            }
            return Optional.of(new PomInfo(parentArtifact, parentVersion, props));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Element child(Element parent, String name) {
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element e && name.equals(e.getNodeName())) return e;
        }
        return null;
    }

    private static String text(Element e) {
        return e == null ? null : e.getTextContent().trim();
    }
}
