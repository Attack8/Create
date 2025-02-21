var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')

// this is terrible, but Forge has forced our hands.
// for some reason, Forge loads half the game with Bootstrap.bootStrap *before* loading mods during datagen.
// this is not the case in other entrypoints.
// this makes mixins to some important places, like BuiltInRegistries, impossible since mixin isn't
// initialized when the class is loaded.
function initializeCoreMod() {
    return {
        "registrycoremod": {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.registries.BuiltInRegistries',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                var CreateBuiltInRegistries = "com/simibubi/create/api/registry/CreateBuiltInRegistries";

                var insn = ASMAPI.buildMethodCall(CreateBuiltInRegistries, "init", "()V", ASMAPI.MethodType.STATIC)

                method.instructions.insertBefore(method.instructions.getLast(), insn)

                return method;
            }
        }
    }
}
