# 构造Form
Geyser的服务端FormUI基于Cumulus，允许开发者直接通过服务端接口生成客户端UI，不需要编写客户端Mod。

基础的用法和 [Geyser官方文档](https://wiki.geysermc.org/geyser/forms/) 中演示的相差不大，只是发送逻辑需要通过baseAPI调用，而不是Floodgate插件

## JAVA插件编写流程

### ModalForm

- 应用场景：
  - ModalForm是最简单的表单形式，但可定制程度较低
  - 仅支持标题、内容和两个按钮
- 具体代码示例如下：
```java
import com.xigua.cumulus.form.ModalForm;
ModalForm.Builder builder = ModalForm.builder()
    .title("Title")
    .content("Content")
    .button1("Button 1")
    .button2("Button 2")
    .validResultHandler((response) -> {
      if (response.clickedButtonId() == 0){
          System.out.println("点击了Button 1");
      }
      else{
          System.out.println("点击了Button 2");
      }
    });
baseAPI.sendForm(player, builder);
```
- 最终效果如下：
![ModalForm](https://mc.163.com/dev/mcmanual/mc-dev/assets/img/1.63321d1c.png)


### SimpleForm

- 应用场景：
  - SimpleForm比ModalForm稍显复杂，但是可定制程度也更高，支持带图片按钮
  - 仅支持标题、内容和不限数量的按钮
- 具体代码示例如下：
```java
import com.xigua.cumulus.form.SimpleForm;
SimpleForm simpleForm = SimpleForm.builder()
        .title("Title")
        .content("Content")
        .button("Button without an image", FormImage.Type.PATH, "")
        .button("Button with URL image", FormImage.Type.URL, "https://github.com/GeyserMC.png?size=200")
        .button("Button with path image", FormImage.Type.PATH, "textures/map/map_background.png")
        .build();
baseAPI.sendForm(player, simpleForm);
```
- 最终效果如下：
![SimpleForm](https://mc.163.com/dev/mcmanual/mc-dev/assets/img/2.b44686c6.png)


### CustomForm

- 应用场景：
  - 最为复杂，但是可定制程度最高
  - 支持标题、内容、标签列表、滑块、输入等等
- 具体代码示例如下：
```java
import com.xigua.cumulus.form.CustomForm;
CustomForm customForm = CustomForm.builder()
        .title("Title")
        .dropdown("Text", "Option 1", "Option 2")
        .input("Input", "placeholder")
        .toggle("Toggle")
        .slider("Text", 0, 10, 1, 5)
        .build();
baseAPI.sendForm(player, customForm);
```
- 最终效果如下：
![ModalForm](https://mc.163.com/dev/mcmanual/mc-dev/assets/img/3.1ddb56e9.png)

## 收到客户端的回复
这很方便，我们可以向客户端发送表单，但我们也希望能够从客户端那里获得响应并能够处理它们。
我们可以用一个（或多个）结果处理器来实现这一点。我们常用的结果处理器有`validResultHandler`或者`closedOrInvalidResultHandler`

```java
SimpleForm simpleForm = SimpleForm.builder()
    .title("选择服务器")
    .content("请选择你想要前往的游戏模式：")
    .button("生存模式") // 索引 0
    .button("创造模式") // 索引 1
    .button("小游戏大厅") // 索引 2
    .closedOrInvalidResultHandler(() -> {
        // 如果玩家关闭了表单，可以执行一些操作，比如返回主城
        System.out.println("玩家关闭了选择服务器表单。");
    })
    .validResultHandler(response -> {
        int buttonId = response.clickedButtonId();
        switch (buttonId) {
            case 0:
                System.out.println("玩家选择了生存模式。");
                // 在这里执行将玩家送往生存服务器的逻辑
                break;
            case 1:
                System.out.println("玩家选择了创造模式。");
                break;
            case 2:
                System.out.println("玩家选择了小游戏大厅。");
                break;
        }
    })
    .build();
```
