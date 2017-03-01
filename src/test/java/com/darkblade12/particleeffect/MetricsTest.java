/***
 * Reflection utilities test to make sure WESV remains cross-version compatible.
 */

package test.java.com.darkblade12.particleeffect;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.comphenix.protocol.metrics.Metrics;

/**
* Tests for the Metrics class.
* @author martinambrus
*
*/
@RunWith(PowerMockRunner.class)
@PrepareForTest(Metrics.class)
public class MetricsTest {

    /**
     * A mock of the Plugin class.
     */
    private Plugin pluginMock;

    /**
     * A mock of the PluginManager class.
     */
    private PluginManager pManagerMock;

    /**
     * A mock of the Server class.
     */
    private Server serverMock;

    /**
     * An actual instance of the Metrics class to be tested.
     */
    private Metrics met;

    /**
     * A blank constructor here for coding style purposes.
     */
    public MetricsTest() {
        // a blank constructor here for coding style purposes
    }

    @Before
    /**
     * This method will setup the testing with mocks of the Server, Plugin and PluginManager
     * classes, as well as an instance of the Metric class.
     *
     * @throws Exception if any of the classes cannot be mocked or Metrics cannot be instantiated.
     */
    public void setUp() throws Exception {
        this.pluginMock = PowerMockito.mock(Plugin.class);
        this.pManagerMock = PowerMockito.mock(PluginManager.class);

        this.serverMock = PowerMockito.mock(Server.class);
        PowerMockito.when(this.serverMock.getPluginManager()).thenReturn(this.pManagerMock);

        PowerMockito.when(this.pluginMock.getServer()).thenReturn(this.serverMock);
        PowerMockito.when(this.pluginMock.getDataFolder()).thenReturn(new File("plugin.yml"));

        this.met = new Metrics(this.pluginMock);
    }

    @Test
    /**
     * Test that we can add a new graph.
     */
    public void testCreateGraph() {
        assertThat("Test graph is not of class Metrics.Graph", this.met.createGraph("test"),
                instanceOf(Metrics.Graph.class));
    }

    @Test
    /**
     * Tests that the new graph retains its given name.
     */
    public void testGraphName() {
        final Metrics.Graph graph = this.met.createGraph("test2");
        assertThat("Test graph did not retain its given name.", graph.getName(), is("test2"));
    }
}