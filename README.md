[![](https://jitpack.io/v/lany192/RxPicker.svg)](https://jitpack.io/#lany192/RxPicker)
## 引用

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

    dependencies {
        implementation 'com.github.lany192:RxPicker:latest.integration'
    }

## 使用

    Disposable subscribe = RxPicker.of()
            .single(false)
            .camera(true)
            .limit(9)
            .start(this)
            .subscribe(new Consumer<List<Image>>() {
                @Override
                public void accept(@NonNull List<Image> images) throws Exception {
                    //处理选择结果
                }
            });