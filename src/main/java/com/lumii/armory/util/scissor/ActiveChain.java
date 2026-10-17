package com.lumii.armory.util.scissor;

import net.minecraft.entity.Entity;

import java.util.UUID;

public record ActiveChain(Entity e1, Entity e2, float length, float duration, UUID id) {
}
