<template>
    <div class="barcode-scanner">
        <!-- 扫描器模态框 -->
        <a-modal :visible="visible" title="条形码扫描" :footer="null" @cancel="handleClose" width="800px">
            <div class="scanner-container">
                <!-- 扫描预览区域 -->
                <!-- 在preview-area中添加 -->
                <div class="preview-area" v-if="showScanner">
                    <video ref="video" class="scanner-video"></video>
                    <canvas ref="canvas" style="display: none;"></canvas>
                    <!-- 添加调试画布 -->
                    <canvas id="drawing-canvas" class="debug-canvas" width="640" height="480"></canvas>
                    <div class="scanning-line" v-if="scanning"></div>
                    
                    <!-- 添加错误提示 -->
                    <div v-if="scannerError" class="scanner-error">
                        <a-alert :message="scannerError" type="error" show-icon />
                        
                        <!-- 添加权限请求按钮 -->
                        <div class="permission-request" v-if="scannerError.includes('权限')">
                            <a-button type="primary" @click="requestCameraPermission" style="margin-top: 10px;">
                                请求摄像头权限
                            </a-button>
                        </div>
                    </div>
                </div>

                <!-- 手动输入区域 -->
                <div class="manual-input" v-else>
                    <a-input-search v-model:value="manualCode" placeholder="请输入ISBN码" enter-button="确认"
                        @search="handleManualInput" />
                </div>

                <!-- 添加文件上传选项 -->
                <div class="upload-option" v-if="showScanner && (scannerError || !isSecureContext)">
                    <a-divider>或者上传条形码图片</a-divider>
                    <a-upload
                        accept="image/*"
                        :showUploadList="false"
                        :beforeUpload="handleImageUpload"
                    >
                        <a-button type="primary">
                            <upload-outlined />
                            上传条形码图片
                        </a-button>
                    </a-upload>
                </div>

                <!-- 切换按钮 -->
                <!-- 在scanner-actions中添加重试按钮 -->
                <div class="scanner-actions">
                    <a-space>
                        <a-button @click="toggleInputMethod">
                            {{ showScanner ? '手动输入' : '扫描输入' }}
                        </a-button>
                        <a-button type="primary" @click="handleStartScan" v-if="showScanner && !scannerError">
                            {{ scanning ? '停止扫描' : '开始扫描' }}
                        </a-button>
                        <!-- 添加重试按钮 -->
                        <a-button type="primary" danger @click="forceReinitScanner" v-if="showScanner && scannerError">
                            重试扫描器
                        </a-button>
                        <!-- 添加一个始终可见的权限请求按钮 -->
                        <a-button type="primary" @click="requestCameraPermission" v-if="showScanner && !scanning">
                            重新获取权限
                        </a-button>
                    </a-space>
                </div>

                <!-- 扫描结果 -->
                <div class="scan-results" v-if="results.length">
                    <a-divider>扫描历史</a-divider>
                    <a-list size="small" :data-source="results">
                        <template #renderItem="{ item }">
                            <a-list-item>
                                <a-space>
                                    <span>{{ item.code }}</span>
                                    <a-tag :color="item.success ? 'success' : 'error'">
                                        {{ item.success ? '有效' : '无效' }}
                                    </a-tag>
                                </a-space>
                            </a-list-item>
                        </template>
                    </a-list>
                </div>
            </div>
        </a-modal>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import Quagga from 'quagga'

const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['update:visible', 'scan'])

// 扫描相关状态
const video = ref(null)
const canvas = ref(null)
const scanning = ref(false)
const showScanner = ref(true)
const manualCode = ref('')
const results = ref([])
const scannerError = ref('') // 新增错误状态

// 添加错误监控
const reportError = (error, context) => {
    console.error(`扫描器错误 [${context}]:`, error);
    // 在生产环境中可以添加错误上报逻辑
    // if (process.env.NODE_ENV === 'production') {
    //     // 发送错误到服务器
    //     // axios.post('/api/error-report', { error: error.message, context, userAgent: navigator.userAgent });
    // }
}

// 检查浏览器兼容性 - 更详细的检测
const checkBrowserCompatibility = () => {
    console.log('检查浏览器兼容性...');

    // 添加polyfill以支持旧版浏览器
    if (navigator.mediaDevices === undefined) {
        navigator.mediaDevices = {};
    }

    // 添加getUserMedia polyfill
    if (navigator.mediaDevices.getUserMedia === undefined) {
        navigator.mediaDevices.getUserMedia = function(constraints) {
            const getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;

            if (!getUserMedia) {
                return Promise.reject(new Error('浏览器不支持getUserMedia API'));
            }

            return new Promise(function(resolve, reject) {
                getUserMedia.call(navigator, constraints, resolve, reject);
            });
        };
    }

    // 检测浏览器类型
    const userAgent = navigator.userAgent;
    const browserInfo = {
        isChrome: /Chrome/.test(userAgent) && !/Edge/.test(userAgent) && !/Edg/.test(userAgent),
        isFirefox: /Firefox/.test(userAgent),
        isEdge: /Edge/.test(userAgent) || /Edg/.test(userAgent),
        isSafari: /Safari/.test(userAgent) && !/Chrome/.test(userAgent),
        isIE: /MSIE|Trident/.test(userAgent)
    };

    console.log('浏览器信息:', browserInfo);

    // 检查是否支持getUserMedia
    if (!navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function') {
        return {
            compatible: false,
            reason: '您的浏览器不支持摄像头访问功能，请使用Chrome、Firefox或Edge等现代浏览器',
            shouldUseAlternative: true
        };
    }

    // 检查是否在安全上下文中运行（HTTPS或localhost）
    const isSecureContext = window.isSecureContext ||
                           window.location.protocol === 'https:' ||
                           window.location.hostname === 'localhost' ||
                           window.location.hostname === '127.0.0.1';

    if (!isSecureContext) {
        return {
            compatible: false,
            reason: '当前网站运行在非HTTPS环境下，已自动切换到替代方案',
            shouldUseAlternative: true
        };
    }

    return { compatible: true };
}

// 切换输入方式
const toggleInputMethod = () => {
    if (scanning.value) {
        stopScanner()
    }
    showScanner.value = !showScanner.value
    scannerError.value = '' // 清除错误信息
}

// 初始化扫描器
const initScanner = async () => {
    console.log('初始化扫描器...')

    // 检查浏览器兼容性
    const compatibility = checkBrowserCompatibility()
    if (!compatibility.compatible) {
        scannerError.value = compatibility.reason
        message.warning(compatibility.reason)

        // 如果应该使用替代方案，自动切换
        if (compatibility.shouldUseAlternative) {
            setTimeout(() => {
                // 自动显示文件上传选项
                scannerError.value = '请使用以下替代方案：上传条形码图片或手动输入'

                // 如果在非安全上下文，显示HTTPS升级建议
                if (window.location.protocol !== 'https:' &&
                    window.location.hostname !== 'localhost' &&
                    window.location.hostname !== '127.0.0.1') {
                    Modal.info({
                        title: '提示',
                        content: '如需使用摄像头扫描功能，请将网站升级到HTTPS或使用localhost本地环境',
                        okText: '我知道了'
                    });
                }
            }, 500);
        }
        return
    }

    // 清除之前的错误
    scannerError.value = ''

    try {
        console.log('尝试获取摄像头权限...')
        console.log('navigator.mediaDevices:', navigator.mediaDevices)

        // 使用更兼容的方式获取摄像头
        let stream;
        try {
            // 尝试使用简单配置，提高兼容性
            stream = await navigator.mediaDevices.getUserMedia({
                video: true,
                audio: false
            });
        } catch (e) {
            console.error('获取摄像头失败:', e);
            throw e;
        }

        console.log('成功获取摄像头权限')

        // 确保视频元素存在并已挂载到DOM
        if (!video.value) {
            throw new Error('视频元素未找到')
        }

        // 手动设置视频流到视频元素
        video.value.srcObject = stream
        await video.value.play().catch(e => {
            console.error('视频播放失败:', e)
            throw new Error('视频播放失败: ' + e.message)
        })

        // 等待视频元素加载完成
        await new Promise((resolve) => {
            if (video.value.readyState >= 2) {
                console.log('视频已准备好')
                resolve()
            } else {
                video.value.onloadeddata = () => {
                    console.log('视频数据已加载')
                    resolve()
                }
                // 添加超时处理
                setTimeout(resolve, 2000)
            }
        })

        console.log('开始初始化Quagga...')

        // 确保视频尺寸已知
        const videoWidth = video.value.videoWidth || 640;
        const videoHeight = video.value.videoHeight || 480;

        console.log('视频尺寸:', videoWidth, 'x', videoHeight);

        // 确保Quagga已正确加载
        if (!Quagga) {
            throw new Error('Quagga库未加载，请检查网络连接或刷新页面');
        }

        if (typeof Quagga.init !== 'function') {
            throw new Error('Quagga库加载不完整，init方法不存在');
        }

        // 添加超时处理，防止Quagga初始化卡住
        const quaggaInitTimeout = setTimeout(() => {
            console.error('Quagga初始化超时');
            scannerError.value = '扫描器初始化超时，请刷新页面重试';
            message.error('扫描器初始化超时');
            
            // 释放摄像头资源
            if (video.value && video.value.srcObject) {
                const tracks = video.value.srcObject.getTracks();
                tracks.forEach(track => track.stop());
                video.value.srcObject = null;
            }
        }, 10000); // 10秒超时

        try {
            Quagga.init({
                inputStream: {
                    name: "Live",
                    type: "LiveStream",
                    target: video.value,
                    constraints: {
                        width: 320,  // 降低分辨率，提高性能
                        height: 240  // 降低分辨率，提高性能
                    },
                    area: { // 扩大扫描区域
                        top: "0%",   // 扫描整个视频区域
                        right: "0%",
                        left: "0%",
                        bottom: "0%"
                    }
                },
                locator: {
                    patchSize: "small",  // 使用small提高性能
                    halfSample: true
                },
                numOfWorkers: 1,  // 减少工作线程，提高稳定性
                frequency: 3,     // 降低扫描频率，减少资源消耗
                decoder: {
                    readers: [
                        "ean_reader",
                        "ean_8_reader",
                        "isbn_reader"  // 只保留必要的解码器
                    ],
                    multiple: false
                },
                locate: true,
                debug: false  // 关闭调试模式，提高性能
            }, (err) => {
                // 清除超时定时器
                clearTimeout(quaggaInitTimeout);
                
                if (err) {
                    console.error('Quagga初始化错误:', err);
                    scannerError.value = `初始化扫描器失败: ${err.message || '未知错误'}`;
                    message.error('初始化扫描器失败，请检查摄像头权限');
                    reportError(err, 'Quagga初始化');

                    // 释放摄像头资源
                    if (video.value && video.value.srcObject) {
                        const tracks = video.value.srcObject.getTracks();
                        tracks.forEach(track => track.stop());
                        video.value.srcObject = null;
                    }
                    return;
                }

                console.log('Quagga初始化成功');
                scanning.value = true; // 自动开始扫描
                scannerError.value = ''; // 清除错误信息
                
                // 重置重初始化标志
                window._scannerReinitialized = false;

                Quagga.onDetected((result) => {
                    if (result && result.codeResult) {
                        console.log('检测到条形码:', result.codeResult.code);
                        handleScanResult(result.codeResult.code);
                    }
                });

                // 添加处理错误的回调
                Quagga.onProcessed((result) => {
                    if (result) {
                        // 在canvas上绘制调试信息
                        const drawingCanvas = document.getElementById('drawing-canvas');
                        if (drawingCanvas) {
                            const ctx = drawingCanvas.getContext('2d');
                            if (ctx) {
                                ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
                                
                                // 如果找到条形码，绘制边界框
                                if (result.boxes) {
                                    result.boxes.forEach(box => {
                                        if (box) {
                                            ctx.strokeStyle = 'green';
                                            ctx.lineWidth = 2;
                                            ctx.beginPath();
                                            ctx.moveTo(box[0][0], box[0][1]);
                                            box.forEach((p, i) => {
                                                if (i > 0) ctx.lineTo(p[0], p[1]);
                                            });
                                            ctx.closePath();
                                            ctx.stroke();
                                        }
                                    });
                                }
                                
                                // 如果有识别结果，显示在界面上
                                if (result.codeResult && result.codeResult.code) {
                                    console.log('实时识别结果:', result.codeResult.code);
                                    // 可以在界面上显示实时识别结果
                                }
                            }
                        }
                    }
                });
            });
        } catch (quaggaError) {
            // 清除超时定时器
            clearTimeout(quaggaInitTimeout);
            
            console.error('Quagga初始化异常:', quaggaError);
            scannerError.value = `扫描器初始化异常: ${quaggaError.message || '未知错误'}`;
            message.error('扫描器初始化异常，请刷新页面重试');
            reportError(quaggaError, 'Quagga初始化异常');
            
            // 释放摄像头资源
            if (video.value && video.value.srcObject) {
                const tracks = video.value.srcObject.getTracks();
                tracks.forEach(track => track.stop());
                video.value.srcObject = null;
            }
        }

        console.log('扫描器启动成功');
        scanning.value = true;
        console.log('扫描器启动成功');
    } catch (error) {
        console.error('摄像头访问错误:', error)
        reportError(error, '摄像头访问');

        // 根据错误类型设置不同的错误信息
        if (error.name === 'NotAllowedError' || error.message.includes('权限被拒绝')) {
            scannerError.value = '摄像头权限被拒绝，请点击下方按钮重新授权'
        } else if (error.name === 'NotFoundError') {
            scannerError.value = '未检测到摄像头设备，请确保您的设备有摄像头并已连接'
        } else if (error.name === 'NotReadableError') {
            scannerError.value = '摄像头可能被其他应用程序占用，请关闭其他使用摄像头的应用'
        } else {
            scannerError.value = `无法访问摄像头: ${error.message || '请确保已授予摄像头权限'}`
        }

        message.error(scannerError.value)

        // 自动切换到手动输入模式
        if (error.name !== 'NotAllowedError') {
            setTimeout(() => {
                showScanner.value = false;
                message.info('已自动切换到手动输入模式');
            }, 3000);
        }
    }
}

// 显示权限请求对话框
const showPermissionDialog = () => {
    // 使用ant-design-vue的Modal组件显示权限请求对话框
    Modal.confirm({
        title: '需要摄像头权限',
        content: '扫描条形码需要访问您的摄像头。请在浏览器弹出的权限请求中选择"允许"。',
        okText: '重新请求权限',
        cancelText: '取消',
        onOk: () => {
            // 重新请求权限
            requestCameraPermission()
        },
        onCancel: () => {
            // 切换到手动输入模式
            showScanner.value = false
            message.info('已切换到手动输入模式')
        }
    })
}

// 请求摄像头权限 - 修改为更强大的版本
const requestCameraPermission = async () => {
    try {
        // 先释放之前的资源
        if (video.value && video.value.srcObject) {
            const tracks = video.value.srcObject.getTracks()
            tracks.forEach(track => track.stop())
            video.value.srcObject = null
        }

        // 重置错误状态
        scannerError.value = '';

        // 显示加载提示
        message.loading('正在请求摄像头权限...', 1);

        // 尝试重新获取摄像头权限
        await navigator.mediaDevices.getUserMedia({
            video: true,
            audio: false
        });

        // 权限获取成功，重新初始化扫描器
        message.success('摄像头权限已授予');

        // 延迟一下再初始化，确保DOM已更新
        setTimeout(() => {
            initScanner();
        }, 500);
    } catch (error) {
        console.error('重新请求权限失败:', error);
        reportError(error, '重新请求权限');

        // 如果用户再次拒绝权限，显示如何手动开启权限的指导
        if (error.name === 'NotAllowedError') {
            showPermissionGuide();
        } else {
            message.error(`无法访问摄像头: ${error.message}`);
            scannerError.value = `无法访问摄像头: ${error.message}`;
        }
    }
}

// 显示权限指导
const showPermissionGuide = () => {
    Modal.info({
        title: '如何开启摄像头权限',
        content: `
            <div>
                <p>您已拒绝摄像头访问权限，请按照以下步骤手动开启：</p>
                <ol>
                    <li>点击浏览器地址栏左侧的锁图标或信息图标</li>
                    <li>在弹出的菜单中找到"摄像头"选项</li>
                    <li>将其设置为"允许"</li>
                    <li>刷新页面后重试</li>
                </ol>
                <p>或者您可以切换到手动输入模式</p>
            </div>
        `,
        okText: '我知道了',
        onOk: () => {
            // 切换到手动输入模式
            showScanner.value = false
            message.info('已切换到手动输入模式')
        }
    })
}

// 处理图片上传
const handleImageUpload = (file) => {
    console.log('开始处理上传的图片:', file.name);
    
    // 显示加载提示
    message.loading('正在处理图片...', 1);
    
    const reader = new FileReader();
    reader.onload = function(e) {
        const src = e.target.result;
        console.log('图片已读取为base64');

        // 创建图片元素
        const image = new Image();
        image.onload = function() {
            console.log('图片已加载，尺寸:', image.width, 'x', image.height);
            
            // 创建canvas
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            canvas.width = image.width;
            canvas.height = image.height;

            // 绘制图片到canvas
            ctx.drawImage(image, 0, 0, image.width, image.height);

            console.log('图片已绘制到canvas，准备解析');

            try {
                // 使用Quagga解析图片
                Quagga.decodeSingle({
                    src: canvas.toDataURL(),
                    numOfWorkers: 0,  // 使用主线程处理
                    inputStream: {
                        size: Math.max(image.width, image.height)
                    },
                    decoder: {
                        readers: [
                            "ean_reader",
                            "ean_8_reader",
                            "code_39_reader",
                            "code_128_reader",
                            "isbn_reader",
                            "upc_reader",
                            "upc_e_reader"
                        ]
                    },
                    locate: true
                }, function(result) {
                    if (result && result.codeResult) {
                        console.log('图片解析成功:', result.codeResult.code);
                        message.success('成功从图片中识别条形码');
                        handleScanResult(result.codeResult.code);
                    } else {
                        console.error('无法从图片中识别条形码');
                        message.error('无法从图片中识别条形码，请尝试更清晰的图片');
                    }
                });
            } catch (error) {
                console.error('图片解析错误:', error);
                message.error('图片解析失败: ' + error.message);
            }
        };
        
        image.onerror = function() {
            console.error('图片加载失败');
            message.error('图片加载失败，请尝试其他图片');
        };
        
        image.src = src;
    };
    
    reader.onerror = function(error) {
        console.error('文件读取错误:', error);
        message.error('文件读取失败');
    };
    
    reader.readAsDataURL(file);
    
    // 阻止默认上传行为
    return false;
};

// 开始扫描
const handleStartScan = () => {
    if (scanning.value) {
        stopScanner()
    } else {
        startScanner()
    }
}

// 启动扫描器
const startScanner = () => {
    try {
        // 检查Quagga是否已正确初始化
        if (!Quagga) {
            throw new Error('扫描器库(Quagga)未加载');
        }

        if (typeof Quagga.start !== 'function') {
            throw new Error('扫描器未正确初始化，Quagga.start不是一个函数');
        }

        // 检查视频元素是否存在
        if (!video.value) {
            throw new Error('视频元素未找到');
        }

        if (!video.value.srcObject) {
            throw new Error('视频流未设置，请重新获取摄像头权限');
        }

        console.log('开始启动扫描器...');
        Quagga.start();
        scanning.value = true;
        console.log('扫描器启动成功');
    } catch (error) {
        console.error('启动扫描器失败:', error);
        reportError(error, '启动扫描器');
        scannerError.value = `启动扫描器失败: ${error.message || '未知错误'}`;
        message.error('启动扫描器失败: ' + error.message);

        // 添加更详细的日志
        console.log('Quagga状态:', Quagga ? '已加载' : '未加载');
        console.log('视频元素状态:', video.value ? '已找到' : '未找到');
        console.log('视频流状态:', video.value && video.value.srcObject ? '已设置' : '未设置');

        // 尝试重新初始化，但添加标志防止无限循环
        if (!window._scannerReinitialized) {
            window._scannerReinitialized = true;
            message.info('正在尝试重新初始化扫描器...');

            // 延迟执行，给UI更新的时间
            setTimeout(() => {
                try {
                    // 先释放资源
                    if (video.value && video.value.srcObject) {
                        const tracks = video.value.srcObject.getTracks();
                        tracks.forEach(track => track.stop());
                        video.value.srcObject = null;
                    }

                    // 重新初始化
                    initScanner();

                    // 重置标志
                    setTimeout(() => {
                        window._scannerReinitialized = false;
                    }, 5000);
                } catch (reinitError) {
                    console.error('重新初始化失败:', reinitError);
                    scannerError.value = `重新初始化失败: ${reinitError.message || '未知错误'}`;
                    message.error('重新初始化失败，请刷新页面或切换到手动输入');

                    // 自动切换到手动输入
                    setTimeout(() => {
                        showScanner.value = false;
                        message.info('已自动切换到手动输入模式');
                    }, 2000);
                }
            }, 1000);
        } else {
            message.error('多次初始化失败，请刷新页面或切换到手动输入');
            // 在onMounted中添加
            onMounted(() => {
                // 检测浏览器是否支持Quagga所需的功能
                const checkBrowserSupport = () => {
                    // 检查是否支持getUserMedia
                    if (!navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function') {
                        scannerError.value = '您的浏览器不支持摄像头访问，请使用Chrome、Firefox或Edge等现代浏览器';
                        message.warning(scannerError.value);
                        return false;
                    }

                    // 检查是否支持Blob
                    if (typeof Blob !== 'function') {
                        scannerError.value = '您的浏览器不支持必要的Web API，请更新浏览器';
                        message.warning(scannerError.value);
                        return false;
                    }

                    return true;
                };

                if (!checkBrowserSupport()) {
                    // 自动切换到手动输入
                    showScanner.value = false;
                }
            })
        }
    }
}

// 停止扫描器
const stopScanner = () => {
    try {
        Quagga.stop()

        // 释放摄像头资源
        if (video.value && video.value.srcObject) {
            const tracks = video.value.srcObject.getTracks()
            tracks.forEach(track => track.stop())
            video.value.srcObject = null
        }

        scanning.value = false
    } catch (error) {
        console.error('停止扫描器失败:', error)
        reportError(error, '停止扫描器');
    }
}

// 处理扫描结果
const handleScanResult = (code) => {
    console.log('处理扫描结果:', code);

    // 清理条形码，移除空格和特殊字符
    const cleanedCode = code.replace(/[\s-]/g, '');
    console.log('清理后的条形码:', cleanedCode);

    // 验证ISBN格式
    const isValidISBN = validateISBN(cleanedCode);
    console.log('ISBN验证结果:', isValidISBN);

    // 播放提示音
    try {
        const beep = new Audio('data:audio/wav;base64,UklGRl9vT19XQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YU...');
        beep.play().catch(e => console.error('播放提示音失败:', e));
    } catch (e) {
        console.error('创建提示音失败:', e);
    }

    // 添加到结果列表
    results.value.unshift({
        code: cleanedCode,
        success: isValidISBN,
        timestamp: new Date().toLocaleTimeString()
    });

    // 如果是有效的ISBN，发送给父组件
    if (isValidISBN) {
        message.success(`成功扫描ISBN: ${cleanedCode}`);
        emit('scan', cleanedCode);
        handleClose();
    } else {
        message.warning(`扫描到的码 ${cleanedCode} 不是有效的ISBN，请重新扫描`);
    }
}

// 验证ISBN格式
const validateISBN = (isbn) => {
    // 移除所有非数字字符
    const cleanedISBN = isbn.replace(/[^0-9X]/gi, '');
    console.log('清理后的ISBN:', cleanedISBN);
    
    // 检查长度 (ISBN-10或ISBN-13)
    if (cleanedISBN.length !== 10 && cleanedISBN.length !== 13) {
        console.log('ISBN长度无效:', cleanedISBN.length);
        return false;
    }
    
    // ISBN-13验证
    if (cleanedISBN.length === 13) {
        // ISBN-13通常以978或979开头
        if (!cleanedISBN.startsWith('978') && !cleanedISBN.startsWith('979')) {
            console.log('ISBN-13前缀无效');
            return false;
        }
        
        // 简单接受所有13位数字，不做校验和验证
        return true;
    } 
    // ISBN-10验证
    else {
        // 简单接受所有10位数字，不做校验和验证
        return true;
    }
}

// 监听visible变化
watch(() => props.visible, (newVal) => {
    if (newVal && showScanner.value) {
        // 当模态框显示且是扫描模式时，初始化扫描器
        // 使用setTimeout确保DOM已经渲染
        setTimeout(() => {
            initScanner()
        }, 300)
    } else if (!newVal && scanning.value) {
        // 当模态框关闭且正在扫描时，停止扫描
        stopScanner()
    }
})

// 关闭扫描器
const handleClose = () => {
    if (scanning.value) {
        stopScanner()
    }
    results.value = []
    scannerError.value = ''
    emit('update:visible', false)
}

// 组件挂载时初始化
onMounted(() => {
    console.log('组件已挂载, visible:', props.visible, 'showScanner:', showScanner.value)
    if (props.visible && showScanner.value) {
        // 延迟初始化，确保DOM已经渲染
        setTimeout(() => {
            initScanner()
        }, 1000) // 增加延迟时间，确保DOM完全渲染
    }
})

// 组件卸载时清理
onUnmounted(() => {
    if (scanning.value) {
        stopScanner()
    }
})
</script>

<style scoped>
.scanner-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.preview-area {
    position: relative;
    width: 100%;
    height: 300px;
    background: #000;
    overflow: hidden;
}

.scanner-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.scanning-line {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: #1890ff;
    animation: scanning 2s linear infinite;
}

.scanner-error {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 80%;
    z-index: 10;
}

.manual-input {
    width: 100%;
    max-width: 300px;
}

.scanner-actions {
    display: flex;
    justify-content: center;
    gap: 10px;
}

.scan-results {
    width: 100%;
    max-height: 200px;
    overflow-y: auto;
}

@keyframes scanning {
    0% {
        top: 0;
    }

    50% {
        top: 100%;
    }

    100% {
        top: 0;
    }
}

.permission-request {
    display: flex;
    justify-content: center;
    margin-top: 10px;
}
</style>

// 强制重新初始化扫描器
const forceReinitScanner = () => {
    console.log('强制重新初始化扫描器');
    
    // 重置错误状态
    scannerError.value = '';
    
    // 显示加载提示
    message.loading('正在重新初始化扫描器...', 1);
    
    // 先停止Quagga
    try {
        if (Quagga && typeof Quagga.stop === 'function') {
            Quagga.stop();
            console.log('Quagga已停止');
        }
    } catch (e) {
        console.error('停止Quagga失败:', e);
    }
    
    // 释放摄像头资源
    if (video.value && video.value.srcObject) {
        const tracks = video.value.srcObject.getTracks();
        tracks.forEach(track => track.stop());
        video.value.srcObject = null;
        console.log('摄像头资源已释放');
    }
    
    // 延迟执行，给UI更新的时间
    setTimeout(() => {
        // 重新请求摄像头权限
        requestCameraPermission();
    }, 1000);
}