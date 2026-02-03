在nacos中创建一个配置文件：`public-config.yaml`，该配置文件被所有微服务共享，内容为:

```yaml
uni:
    api:
        # 是否启用自动扫描API并添加到Redis功能
        scanner:
            enabled: true
auth:
    # 微服务内部密钥，业务服务会校验请求是否携带这个，避免绕过网关直接请求内部服务
    # 内部服务校验请求头：X-Inner-Token
    internal:
        secret: xC9D9HgFrjye4jjAbzLwXsUQKpVfFPXx
```