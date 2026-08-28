// Insert in a fragment program that has reconstructed camera-relative position.
// GLSL 1.20 compatible; loop bound is constant for legacy drivers.
uniform int vclLightCount;
uniform vec4 vclLightPosRadius[32];
uniform vec4 vclLightColorPower[32];

vec3 vclApplyLights(vec3 cameraRelativePos, vec3 normal) {
    vec3 result = vec3(0.0);
    for (int i = 0; i < 32; ++i) {
        if (i >= vclLightCount) break;
        vec3 delta = vclLightPosRadius[i].xyz - cameraRelativePos;
        float radius = vclLightPosRadius[i].w;
        float distanceToLight = length(delta);
        float attenuation = max(0.0, 1.0 - distanceToLight / radius);
        attenuation *= attenuation;
        float diffuse = max(0.12, dot(normalize(delta), normal));
        result += vclLightColorPower[i].rgb * (vclLightColorPower[i].a * attenuation * diffuse);
    }
    return result;
}
