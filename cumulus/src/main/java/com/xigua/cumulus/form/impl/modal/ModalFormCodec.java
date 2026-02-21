/*
 * Copyright (c) 2020-2023 GeyserMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Cumulus
 */
package com.xigua.cumulus.form.impl.modal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import com.xigua.cumulus.form.ModalForm;
import com.xigua.cumulus.form.util.FormType;
import com.xigua.cumulus.form.util.impl.FormCodecImpl;
import com.xigua.cumulus.response.ModalFormResponse;
import com.xigua.cumulus.response.impl.ModalFormResponseImpl;
import com.xigua.cumulus.response.result.FormResponseResult;
import com.xigua.cumulus.util.JsonUtils;

public class ModalFormCodec extends FormCodecImpl<ModalForm, ModalFormResponse> {
  ModalFormCodec() {
    super(ModalForm.class, FormType.MODAL_FORM);
  }

  @Override
  public ModalForm deserializeForm(JsonObject source, JsonDeserializationContext context) {
    String title = JsonUtils.assumeMember(source, "title").getAsString();
    String content = JsonUtils.assumeMember(source, "content").getAsString();
    String button1 = JsonUtils.assumeMember(source, "button1").getAsString();
    String button2 = JsonUtils.assumeMember(source, "button2").getAsString();
    return new ModalFormImpl(title, content, button1, button2);
  }

  @Override
  public void serializeForm(ModalForm form, JsonSerializationContext context, JsonObject result) {
    result.addProperty("title", form.title());
    result.addProperty("content", form.content());
    result.addProperty("button1", form.button1());
    result.addProperty("button2", form.button2());
  }

  @Override
  public FormResponseResult<ModalFormResponse> deserializeResponse(
      @NonNull ModalForm form, @NonNull String responseData) {

    responseData = responseData.trim();

    if ("true".equals(responseData)) {
      return FormResponseResult.valid(ModalFormResponseImpl.of(0, form.button1()));
    } else if ("false".equals(responseData)) {
      return FormResponseResult.valid(ModalFormResponseImpl.of(1, form.button2()));
    }
    return FormResponseResult.invalid(-1, "Response wasn't a boolean");
  }
}
