# Custom Blocks
Allows registration of custom blocks and items through NMS registries on Paper.

## Notes
namespace = Identifier = ResourceLocation

This code assumes that the client has a namespace for the created custom block namespace.
You CANNOT register custom blocks with a vanilla client and/or no resource packs.
The client will not accept the custom block namespace and automatically disconnect from the server if it cannot find the associated custom block namespace that the server sent.


The .json files uses the CODEC of `net.minecraft.world.level.block.state.BlockBehaviour.Properties#CODEC` so read the method names from the Properties class to adequately adjust the properties of the custom block.

The custom item registry does not have a CODEC at the time of writting this.

Any file prefixed with `-` hyphen symbol will be considered disabled, and not be registered.

Currently built for 1.21.4, should work on future versions assuming Mojang doesn't change the registry methods.
