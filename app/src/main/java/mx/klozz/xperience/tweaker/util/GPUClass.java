package mx.klozz.xperience.tweaker.util;
/*
 * XPerience Kernel Tweaker - An Android CPU Control application
 * Copyright (C) 2011-2015 Carlos "Klozz" Jesus <TeamMEX@XDA-Developers>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
import android.util.Log;
import java.io.File;

import mx.klozz.xperience.tweaker.helpers.Helpers;

public class GPUClass implements Constants {
    private String clkpath = null;
    private String clkvals = "";

    public GPUClass() {
        gpu_clk();
        Log.d(TAG, "detect gpu freq path: " + this.clkpath);
    }

    public String gpuclk_path() {
        return this.clkpath;
    }

    public CharSequence[] gpuclk_values() {
        return this.clkvals.split("\\s");
    }

    public CharSequence[] gpuclk_names() {
        CharSequence[] v = gpuclk_values();
        for (int i = 0; i < v.length; i++) {
            v[i] = Helpers.toMHz(String.valueOf(Integer.parseInt(v[i].toString()) / 1000));
        }
        return v;
    }

    public String gpugovset_path() {
        return gpu_gov_param_path();
    }

    private void gpu_clk() {
        CMDProcessor.CommandResult cr = null;
        if (new File("/sys/class/kgsl/kgsl-3d0/max_gpuclk").exists()) {
            if (new File("/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies").exists()) {
                this.clkpath = "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
                this.clkvals = Helpers.LeerUnaLinea("/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies");
            }
        } else if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk").exists()) {
            if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies").exists()) {
                this.clkpath = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
                this.clkvals = Helpers.LeerUnaLinea("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies");
            } else {
                cr = new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep 8960 /system/build.prop`");
                if (cr.success() && cr.stdout.contains("8960")) {
                    this.clkpath = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
                    this.clkvals = "128000000 200000000 300000000 325000000 400000000 480000000 487500000";
                } else {
                    cr = new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep 8660 /system/build.prop`");
                    if (cr.success() && cr.stdout.contains("8660")) {
                        this.clkpath = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
                        this.clkvals = "177000000 200000000 228571000 266667000 300000000 320000000";
                    }
                }
            }
        } else if (new File("/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit").exists()) {
            cr = new CMDProcessor().sh.runWaitFor("busybox echo `busybox find /sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit -perm -600`");
            if (cr.success() && cr.stdout.contains("/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit")) {
                this.clkpath = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit";
                this.clkvals = Helpers.LeerUnaLinea("/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_list");
            }
        }

    }

    private String gpu_gov_param_path() {
        if (new File("/sys/module/msm_kgsl_core/parameters").exists()) {
            return "/sys/module/msm_kgsl_core/parameters";
        } else if (new File("/sys/kernel/debug/tegra_host/scaling").exists()) {
            return "/sys/kernel/debug/tegra_host/scaling";
        } else {
            return null;
        }
    }

    private String gpu_gov_path() {//Direccion donde encotraremos los gobernadores de la CPU
        if (new File("/sys/kernel/pvr_simple_gov/simple_governor").exists()) {
            return "/sys/kernel/pvr_simple_gov/simple_governor";
        } else if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor").exists()) {
            return "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
        } else if (new File("/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor").exists()) {
            return "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/pwrscale/trustzone/governor";
        } else {
            return null;
        }
    }
}
