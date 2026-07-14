- `start` and `stop` run while GPU's OpenGL context is current, so the extension can own its GL resources.
- `drawSkybox` can replace RuneLite's normal sky.
- `drawSkyboxOverlay` can draw extra sky layers.
- `drawPostScene` can draw weather against scene depth.
- `sceneFragmentShader` adds an optional function immediately before fog is mixed.

```java
gpuRegistration = gpuApi.register(new GpuApi.Extension()
{
    public void start()
    {
        // Create OpenGL resources.
    }

    public boolean drawSkybox(GpuApi.Frame frame)
    {
        // Draw a custom sky and return true to replace RuneLite's sky.
        return true;
    }

    public void drawPostScene(GpuApi.Frame frame)
    {
        // Draw weather using the current scene depth buffer.
    }

    public void stop()
    {
        // Delete OpenGL resources.
    }
});
```

The optional scene function receives world position, distance, view direction, surface normal, fog amount, and whether the pixel is terrain or a model. It may change `surfaceColor` and `fogColor`. `updateScene` receives the bound scene-program ID so the extension can update its uniforms.

The first version accepts one extension. More extension slots or render stages can be added later if there is a real use for them.

## Packaging note

A separately distributed plugin needs the small public `GpuApi.java` contract in a shared parent classloader such as `runelite-client`. GPU keeps the implementation.
