/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2020 Abex
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
package info.sigterm.plugins.gpue;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import info.sigterm.plugins.gpue.template.Template;
import static net.runelite.opengl.GL.GL_COMPILE_STATUS;
import static net.runelite.opengl.GL.GL_FALSE;
import static net.runelite.opengl.GL.GL_LINK_STATUS;
import static net.runelite.opengl.GL.GL_TRUE;
import static net.runelite.opengl.GL.GL_VALIDATE_STATUS;

public class Shader
{
	@VisibleForTesting
	final List<Unit> units = new ArrayList<>();

	@RequiredArgsConstructor
	@VisibleForTesting
	static class Unit
	{
		@Getter
		private final int type;

		@Getter
		private final String filename;
	}

	public Shader()
	{
	}

	public info.sigterm.plugins.gpue.Shader add(int type, String name)
	{
		units.add(new Unit(type, name));
		return this;
	}

	public int compile(RuneLiteGL gl, Template template) throws ShaderException
	{
		int program = gl.glCreateProgram();
		int[] shaders = new int[units.size()];
		int i = 0;
		boolean ok = false;
		try
		{
			while (i < shaders.length)
			{
				Unit unit = units.get(i);
				int shader = gl.glCreateShader(unit.type);
				if (shader == 0)
				{
					throw new ShaderException("Unable to create shader of type " + unit.type);
				}

				String source = template.load(unit.filename);
				gl.glShaderSource(shader, source);
				gl.glCompileShader(shader);

				if (gl.glGetShaderiv(shader, GL_COMPILE_STATUS) != GL_TRUE)
				{
					String err = gl.glGetShaderInfoLog(shader);
					gl.glDeleteShader(shader);
					throw new ShaderException(err);
				}
				gl.glAttachShader(program, shader);
				shaders[i++] = shader;
			}

			gl.glLinkProgram(program);

			if (gl.glGetProgram(program, GL_LINK_STATUS) == GL_FALSE)
			{
				String err = gl.glGetProgramInfoLog(program);
				throw new ShaderException(err);
			}

			gl.glValidateProgram(program);

			if (gl.glGetProgram(program, GL_VALIDATE_STATUS) == GL_FALSE)
			{
				String err = gl.glGetProgramInfoLog(program);
				throw new ShaderException(err);
			}

			ok = true;
		}
		finally
		{
			while (i > 0)
			{
				int shader = shaders[--i];
				gl.glDetachShader(program, shader);
				gl.glDeleteShader(shader);
			}

			if (!ok)
			{
				gl.glDeleteProgram(program);
			}
		}

		return program;
	}
}
