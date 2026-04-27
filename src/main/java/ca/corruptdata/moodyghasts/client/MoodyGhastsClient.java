package ca.corruptdata.moodyghasts.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "moodyghasts", dist = Dist.CLIENT)
public class MoodyGhastsClient {

    public MoodyGhastsClient(ModContainer container) {
        // Register the built-in configuration screen
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}