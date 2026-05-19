# Agent 形象设计提示词

## 中文图像生成提示词

一位拟人化温度计助手，身体呈温度计形状，上半部分有可爱的圆形眼睛和小嘴，下半部分是红色水银柱。整体配色以蓝色、白色和浅红色为主，背景为深蓝色科技渐变，带有微弱发光效果。风格简洁可爱，适合大学课程项目 UI 展示。角色名字叫"温度计"，身体上有刻度标记。2D 扁平化风格，干净线条，柔和阴影。

## 英文图像生成提示词

An anthropomorphic thermometer assistant mascot, body shaped like a thermometer, with cute round eyes and a small smile on the upper part, red mercury column on the lower part. Color palette: blue, white, and light red. Dark blue gradient tech background with soft glow effects. Clean and cute design style, suitable for university capstone project UI. Character name is "Thermometer" with scale markings on the body. 2D flat illustration style, clean lines, soft shadows.

## 透明背景版本提示词

一个拟人化温度计吉祥物，透明背景。身体呈温度计形状，顶部有可爱的卡通脸（圆形眼睛、微笑），红色水银柱在身体下方。蓝白红配色，扁平化设计，干净线条。适合作为网页图标或头像使用。

## Web 吉祥物版本提示词

A cute thermometer mascot for a web application, isolated on transparent background. Thermometer-shaped body with adorable cartoon face (round eyes, gentle smile), red indicator column. Blue, white and red color scheme. Flat design with clean outlines. Suitable for use as a web app mascot or avatar. Front-facing, centered composition.

## 图标版本提示词

A minimalist thermometer icon with a small cute face, circular design, suitable for app icon or favicon. Simple shapes, bold colors, recognizable at small sizes. Blue and red gradient, white background circle.

---

## 当前实现

v2.0 使用内联 SVG fallback 作为 Agent 形象展示。SVG 包含：
- 蓝色到红色渐变温度计柱体
- 红色圆形底部（水银球）
- 发光滤镜效果
- 白色高光点

如后续生成了 PNG 图片，放置于 `docs/images/agent-thermometer.png`，前端将自动切换显示。
