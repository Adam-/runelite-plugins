/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, BegOsrs <https://github.com/begosrs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package begosrs.barbarianassault.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.VarPlayer;

/**
 * Server controlled "content-developer" integers.
 *
 * @see VarPlayer
 * <p>
 * These differ from a {@link VarPlayer} in that VarBits can be
 * less than 32 bits. One or more VarBits can be assigned to a
 * backing VarPlayer, each with a static range of bits that it is
 * allowed to access. This allows a more compact representation
 * of small values, like booleans
 */
@AllArgsConstructor
@Getter
public enum BaVarbits
{

	BA_CANNON_GREEN_EGGS(3257),
	BA_CANNON_RED_EGGS(3258),
	BA_CANNON_BLUE_EGGS(3259),
	BA_CANNON_OMEGA_EGGS(3266),
	BA_ATTACKER_ROLE_BASE_POINTS(4759),
	BA_COLLECTOR_ROLE_BASE_POINTS(4760),
	BA_HEALER_ROLE_POINTS(4761),
	BA_DEFENDER_ROLE_BASE_POINTS(4762),
	BA_ATTACKER_ROLE_MULTIPLIER(4763),
	BA_COLLECTOR_ROLE_MULTIPLIER(4764),
	BA_HEALER_ROLE_MULTIPLIER(4765),
	BA_DEFENDER_ROLE_MULTIPLIER(4766),
	;

	/**
	 * The raw varbit ID.
	 */
	private final int id;
}
