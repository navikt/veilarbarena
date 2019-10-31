package no.nav.fo.veilarbarena;

import lombok.SneakyThrows;
import no.nav.fo.veilarbarena.utils.NaiseratorUtils;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static no.nav.fo.veilarbarena.utils.NaiseratorUtils.CONFIG_MAPS_PATH;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class NaiseratorUtilsTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Test
    @SneakyThrows
    public void readFileContent() {
        createFolder("foo", "bar");
        writeFile("foo/bar/baz", "the content");
        String fileContent = NaiseratorUtils.getFileContent(tempPath("foo/bar/baz"));
        assertThat(fileContent).isEqualTo("the content");
    }

    @Test
    public void readCredentialsWithDefaultFileNames() {
        createFolder("creds");
        writeFile("creds/username", "the username");
        writeFile("creds/password", "the password");
        NaiseratorUtils.Credentials credentials = NaiseratorUtils.getCredentials(tempPath("creds"));
        assertThat(credentials.username).isEqualTo("the username");
        assertThat(credentials.password).isEqualTo("the password");
    }

    @Test
    public void readCredentialsWithCustomFileNames() {
        createFolder("creds");
        writeFile("creds/un", "the username");
        writeFile("creds/pw", "the password");
        NaiseratorUtils.Credentials credentials = NaiseratorUtils.getCredentials(tempPath("creds"), "un", "pw");
        assertThat(credentials.username).isEqualTo("the username");
        assertThat(credentials.password).isEqualTo("the password");
    }

    @Test
    public void defaultPath() {
        assertThat(NaiseratorUtils.getDefaultSecretPath("path")).isEqualTo("/var/run/secrets/nais.io" + "/path");
    }

    @Test
    public void readConfigMap() {
        createFolder("configMaps", "configMap");
        systemPropertiesRule.setProperty(CONFIG_MAPS_PATH, tempPath("configMaps"));

        writeFile("configMaps/configMap/KEY_1", "VALUE 1");
        writeFile("configMaps/configMap/KEY_2", "VALUE 2");
        writeFile("configMaps/configMap/KEY_3", "VALUE 3");

        Map<String, String> configMap = NaiseratorUtils.readConfigMap("configMap");


        assertThat(configMap).containsOnlyKeys("KEY_1", "KEY_2", "KEY_3");
        assertThat(configMap.get("KEY_1")).isEqualTo("VALUE 1");
        assertThat(configMap.get("KEY_2")).isEqualTo("VALUE 2");
        assertThat(configMap.get("KEY_3")).isEqualTo("VALUE 3");
    }

    @Test
    public void cherryPickFromConfigMap() {
        createFolder("configMaps", "configMap");
        systemPropertiesRule.setProperty(CONFIG_MAPS_PATH, tempPath("configMaps"));

        writeFile("configMaps/configMap/KEY_1", "VALUE 1");
        writeFile("configMaps/configMap/KEY_2", "VALUE 2");
        writeFile("configMaps/configMap/KEY_3", "VALUE 3");

        Map<String, String> configMap = NaiseratorUtils.readConfigMap("configMap", "KEY_1", "KEY_3");

        assertThat(configMap).containsOnlyKeys("KEY_1", "KEY_3");
        assertThat(configMap.get("KEY_1")).isEqualTo("VALUE 1");
        assertThat(configMap.get("KEY_3")).isEqualTo("VALUE 3");
    }

    @Test(expected = IllegalStateException.class)
    public void cherryPickFromConfigMapFailsWhenKeyIsNotFound() {
        createFolder("configMaps", "configMap");
        systemPropertiesRule.setProperty(CONFIG_MAPS_PATH, tempPath("configMaps"));

        writeFile("configMaps/configMap/KEY_1", "VALUE 1");
        writeFile("configMaps/configMap/KEY_2", "VALUE 2");
        writeFile("configMaps/configMap/KEY_3", "VALUE 3");

        Map<String, String> configMap = NaiseratorUtils.readConfigMap("configMap", "KEY_1", "KEY_4");
    }


    @SneakyThrows
    private void createFolder(String... folderNames) {
        tmp.newFolder(folderNames);
    }

    @SneakyThrows
    private void writeFile(String path, String content) {
        Files.write(Paths.get(tempPath(path)), Collections.singletonList(content));
    }

    private String tempPath(String path) {
        return tmp.getRoot().getAbsolutePath() + "/" + path;
    }
}
